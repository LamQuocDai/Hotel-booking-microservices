using Application.DTOs;
using Microsoft.AspNetCore.Http;

namespace Application.Interfaces
{
    public interface IImageServive
    {
        Task<ApiResponseDto<ImageDto>> UploadFileAsync(Guid roomId, IFormFile file);
        Task<ApiResponseDto<bool>> DeleteFileAsync(Guid id);
        Task<ApiResponseDto<ImageDto>> GetImageByIdAsync(Guid id);
    }
}
