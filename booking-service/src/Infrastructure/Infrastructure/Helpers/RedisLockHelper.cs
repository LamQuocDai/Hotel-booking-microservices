using StackExchange.Redis;

namespace Infrashtructure.Helpers
{
    /// <summary>
    /// Redis distributed lock helper for preventing race conditions in distributed systems.
    /// Uses Redis SET with NX (not exists) and PX (milliseconds expiry) options.
    /// Implements proper lock release using Lua script to ensure only the lock owner can release it.
    /// </summary>
    public class RedisLockHelper
    {
        private readonly IConnectionMultiplexer _redis;
        private readonly IDatabase _db;

        public RedisLockHelper(IConnectionMultiplexer redis)
        {
            _redis = redis;
            _db = redis.GetDatabase();
        }

        /// <summary>
        /// Attempts to acquire a distributed lock.
        /// </summary>
        /// <param name="lockKey">The Redis key for the lock (e.g., "lock:room:{roomId}")</param>
        /// <param name="lockValue">Unique identifier for this lock instance (prevents accidental release by another process)</param>
        /// <param name="expiryMilliseconds">Lock expiry time in milliseconds (default: 10000ms = 10s)</param>
        /// <returns>True if lock acquired, false otherwise</returns>
        public async Task<bool> AcquireLockAsync(string lockKey, string lockValue, int expiryMilliseconds = 10000)
        {
            // SET key value NX PX milliseconds
            // NX: Only set if key does not exist
            // PX: Set expiry time in milliseconds
            return await _db.StringSetAsync(
                lockKey, 
                lockValue, 
                TimeSpan.FromMilliseconds(expiryMilliseconds), 
                When.NotExists);
        }

        /// <summary>
        /// Releases a distributed lock using Lua script to ensure atomicity.
        /// Only releases the lock if the stored value matches the provided value.
        /// This prevents accidental release of a lock acquired by another process.
        /// </summary>
        /// <param name="lockKey">The Redis key for the lock</param>
        /// <param name="lockValue">The unique identifier used when acquiring the lock</param>
        /// <returns>True if lock was released, false if lock didn't exist or value didn't match</returns>
        public async Task<bool> ReleaseLockAsync(string lockKey, string lockValue)
        {
            // Lua script to atomically check value and delete if matches
            // This ensures we only delete our own lock
            const string script = @"
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end";

            var result = await _db.ScriptEvaluateAsync(
                script,
                new RedisKey[] { lockKey },
                new RedisValue[] { lockValue });

            return (int)result == 1;
        }

        /// <summary>
        /// Helper method to execute a function with automatic lock acquisition and release.
        /// Implements the try-finally pattern to ensure lock is always released.
        /// </summary>
        /// <typeparam name="T">Return type of the function</typeparam>
        /// <param name="lockKey">The Redis key for the lock</param>
        /// <param name="action">The function to execute while holding the lock</param>
        /// <param name="expiryMilliseconds">Lock expiry time in milliseconds</param>
        /// <param name="throwIfLockFails">If true, throws exception when lock cannot be acquired</param>
        /// <returns>Result of the action, or default(T) if lock couldn't be acquired and throwIfLockFails is false</returns>
        public async Task<T?> ExecuteWithLockAsync<T>(
            string lockKey, 
            Func<Task<T>> action, 
            int expiryMilliseconds = 10000,
            bool throwIfLockFails = true)
        {
            var lockValue = Guid.NewGuid().ToString(); // Unique identifier for this lock instance
            var lockAcquired = await AcquireLockAsync(lockKey, lockValue, expiryMilliseconds);

            if (!lockAcquired)
            {
                if (throwIfLockFails)
                {
                    throw new InvalidOperationException($"Failed to acquire lock: {lockKey}");
                }
                return default;
            }

            try
            {
                return await action();
            }
            finally
            {
                // Always attempt to release the lock
                await ReleaseLockAsync(lockKey, lockValue);
            }
        }
    }
}
