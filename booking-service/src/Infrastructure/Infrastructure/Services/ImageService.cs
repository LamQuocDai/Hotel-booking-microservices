
using Application.Constants;
using Application.DTOs;
using Application.Interfaces;
using AutoMapper;
using Domain.Entities;
using Infrashtructure.Data;
using Microsoft.AspNetCore.Http;
using System.Linq;

namespace Infrashtructure.Services
{
    public class ImageService : IImageServive
    {
        private readonly BookingDbContext _context;
        private readonly IMapper _mapper;
        private readonly IFileStorageService _fileStorageService;

        // Allowed file types
        private readonly HashSet<string> _allowedExtensions = new()
        {
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", // Images
            ".pdf", ".doc", ".docx", ".txt", ".rtf", // Documents
            ".mp4", ".avi", ".mov", ".wmv", ".flv", // Videos
            ".mp3", ".wav", ".ogg", ".m4a" // Audio
        };

        private readonly long _maxFileSize = 10 * 1024 * 1024; // 10MB
        public ImageService(BookingDbContext context, IMapper mapper, IFileStorageService fileStorageService)
        {
            _context = context;
            _mapper = mapper;
            _fileStorageService = fileStorageService;
        }

        public async Task<ApiResponseDto<ImageDto>> UploadFileAsync(Guid roomId, IFormFile file)
        {
            try
            {
                // Validate file
                var validationResult = ValidateFile(file);
                if (!validationResult.IsValid)
                {
                    return new ApiResponseDto<ImageDto>
                    {
                        IsSuccess = false,
                        Message = validationResult.ErrorMessage,
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }

                // Upload file to S3 storage
                using var stream = file.OpenReadStream();
                var (isSuccess, errorMessage, fileInfo) = await _fileStorageService.UploadFileAsync(
                    stream,
                    file.FileName,
                    file.ContentType);

                if (!isSuccess || fileInfo == null)
                {
                    return new ApiResponseDto<ImageDto>
                    {
                        IsSuccess = false,
                        Message = errorMessage ?? "Failed to upload file to storage",
                        StatusCode = ApplicationStatusCode.InternalServerError
                    };
                }

                // Save file information to database
                var uploadFileEntity = new Image
                {
                    Id = Guid.NewGuid(),
                    OriginalFilename = fileInfo.OriginalFilename,
                    Filename = fileInfo.Filename,
                    Filesize = fileInfo.Filesize,
                    FileType = fileInfo.FileType,
                    FullPath = fileInfo.FullPath,
                    RoomId = roomId,
                };

                _context.Images.Add(uploadFileEntity);
                await _context.SaveChangesAsync();

                var uploadFileDto = _mapper.Map<ImageDto>(uploadFileEntity);

                return new ApiResponseDto<ImageDto>
                {
                    IsSuccess = true,
                    Data = uploadFileDto,
                    Message = "File uploaded successfully to S3 and saved to database",
                    StatusCode = ApplicationStatusCode.Created
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<ImageDto>
                {
                    IsSuccess = false,
                    Message = $"Failed to upload file: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        public async Task<ApiResponseDto<bool>> DeleteFileAsync(Guid fileId)
        {
            try
            {
                var fileEntity = await _context.Images.FindAsync(fileId);
                if (fileEntity == null)
                {
                    return new ApiResponseDto<bool>
                    {
                        IsSuccess = false,
                        Message = "File not found",
                        StatusCode = ApplicationStatusCode.NotFound
                    };
                }

                // Delete from S3 storage
                var deleted = await _fileStorageService.DeleteFileAsync(fileEntity.Filename);
                if (!deleted)
                {
                    _logger.LogWarning("Failed to delete file from S3 storage: {FileName}", fileEntity.Filename);
                }

                // Delete from database
                _context.Images.Remove(fileEntity);
                await _context.SaveChangesAsync();

                return new ApiResponseDto<bool>
                {
                    IsSuccess = true,
                    Data = true,
                    Message = "File deleted successfully from both S3 storage and database",
                    StatusCode = ApplicationStatusCode.Success
                };
            }
            catch (Exception ex)
            { 
                return new ApiResponseDto<bool>
                {
                    IsSuccess = false,
                    Message = $"Failed to delete file: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        public async Task<ApiResponseDto<ImageDto>> GetUploadFileByIdAsync(Guid id)
        {
            try
            {
                var uploadFile = await _context.Images.FindAsync(id);

                if (uploadFile == null)
                {
                    return new ApiResponseDto<ImageDto>()
                    {
                        IsSuccess = false,
                        Message = "File not found.",
                        StatusCode = ApplicationStatusCode.NotFound
                    };
                }

                return new ApiResponseDto<ImageDto>
                {
                    IsSuccess = true,
                    Data = _mapper.Map<ImageDto>(uploadFile),
                    Message = "File retrieved successfully.",
                    StatusCode = ApplicationStatusCode.Success,
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<ImageDto>
                {
                    IsSuccess = false,
                    Message = $"Failed to retrieve file: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        private (bool IsValid, string ErrorMessage) ValidateFile(IFormFile file)
        {
            if (file == null || file.Length == 0)
            {
                return (false, "No file provided");
            }

            if (file.Length > _maxFileSize)
            {
                return (false, $"File size exceeds maximum allowed size of {_maxFileSize / (1024 * 1024)}MB");
            }

            var extension = Path.GetExtension(file.FileName).ToLowerInvariant();
            if (!_allowedExtensions.Contains(extension))
            {
                return (false, $"File type '{extension}' is not allowed. Allowed types: {string.Join(", ", _allowedExtensions)}");
            }

            return (true, string.Empty);
        }
    }
}

