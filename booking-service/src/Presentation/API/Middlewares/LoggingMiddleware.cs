using System.Diagnostics;

namespace API.Middlewares
{
    public class LoggingMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly ILogger<LoggingMiddleware> _logger;

        public LoggingMiddleware(RequestDelegate next, ILogger<LoggingMiddleware> logger)
        {
            _next = next;
            _logger = logger;
        }

        public async Task InvokeAsync(HttpContext context)
        {
            var stopwatch = Stopwatch.StartNew();

            // log request info
            _logger.LogInformation($"[Request] {context.Request.Method} {context.Request.Path}");

            await _next(context); // go to next middleware

            stopwatch.Stop();

            // log response info
            _logger.LogInformation($"[Response] {context.Response.StatusCode} - Took {stopwatch.ElapsedMilliseconds} ms");
        }
    }

    public static class LoggingMiddlewareExtensions
    {
        public static IApplicationBuilder UseRequestLogging(this IApplicationBuilder builder)
        {
            return builder.UseMiddleware<LoggingMiddleware>();
        }
    }
}
