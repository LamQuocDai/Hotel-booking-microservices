
using Application.DTOs;

namespace Application.Interfaces
{
    public interface IFileStorageService
    {
        Task<(bool IsSuccess, string? ErrorMessage, ImageDto? ImageInfo)> UploadFileAsync(Stream fileStream, string originalFileName, string contentType);
        Task<bool> DeleteFileAsync(string fileName);
        string GetFileUrl(string fileName);
    }
}
