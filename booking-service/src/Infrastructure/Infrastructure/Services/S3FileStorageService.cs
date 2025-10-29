using Application.Interfaces;
using Application.DTOs;
using Microsoft.Extensions.Configuration;
using Amazon.S3;
using Amazon.S3.Model;

namespace Infrashtructure.Services
{
    public class S3FileStorageService : IFileStorageService
    {
        private readonly IConfiguration _configuration;
        private readonly IAmazonS3 _s3Client;
        private readonly string _bucketName;

        public S3FileStorageService(IConfiguration configuration, IAmazonS3 s3Client)
        {
            _configuration = configuration;
            _s3Client = s3Client;
            _bucketName = _configuration["AWS:BucketName"] ?? throw new ArgumentNullException("AWS Bucket Name is not configured.");
        }


        public async Task<(bool IsSuccess, string? ErrorMessage, ImageDto? ImageInfo)> UploadFileAsync(Stream fileStream, string originalFileName, string contentType)
        {
            try
            {
                // Generate unique file name
                var fileExtension = Path.GetExtension(originalFileName);
                var uniqueFileName = $"{Guid.NewGuid()}{fileExtension}";
                var key = $"uploads-booking/{DateTime.UtcNow:yyyy-MM-dd}/{uniqueFileName}";

                var request = new PutObjectRequest
                {
                    BucketName = _bucketName,
                    Key = key,
                    InputStream = fileStream,
                    ContentType = contentType,
                    ServerSideEncryptionMethod = ServerSideEncryptionMethod.AES256,
                    CannedACL = S3CannedACL.PublicRead // Make file publicly readable
                };

                var response = await _s3Client.PutObjectAsync(request);

                if (response.HttpStatusCode == System.Net.HttpStatusCode.OK)
                {
                    var fileUrl = GetFileUrl(key);

                    var fileInfo = new ImageDto
                    {
                        Id = Guid.NewGuid(), // This will be overridden when saving to database
                        OriginalFilename = originalFileName,
                        Filename = key,
                        Filesize = (int)fileStream.Length,
                        FileType = contentType,
                        FullPath = fileUrl,
                        CreatedAt = DateTime.UtcNow
                    };

                    return (true, null, fileInfo);
                }
                else
                {
                    return (false, "Failed to upload file to storage", null);
                }
            }
            catch (Exception ex)
            {
                return (false, $"Error uploading file: {ex.Message}", null);
            }
        }

        public async Task<bool> DeleteFileAsync(string fileName)
        {
            try
            {
                var request = new DeleteObjectRequest
                {
                    BucketName = _bucketName,
                    Key = fileName
                };

                var response = await _s3Client.DeleteObjectAsync(request);
                return response.HttpStatusCode == System.Net.HttpStatusCode.NoContent;
            }
            catch (Exception ex)
            {
                return false;
            }
        }   

        public string GetFileUrl(string fileName)
        {
            var region = _configuration["AWS:Region"] ?? "us-east-1";
            return $"https://{_bucketName}.s3.{region}.amazonaws.com/{fileName}";
        }
    }
}
