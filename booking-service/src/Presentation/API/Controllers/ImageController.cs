using Application.DTOs;
using Application.Interfaces;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers
{
    [Route("api/images")]
    [ApiController]
    public class ImageController : ControllerBase
    {
        private readonly IImageServive _imageService;
        public ImageController(IImageServive imageService)
        {
            _imageService = imageService;
        }

        [HttpPost("upload")]
        public async Task<ActionResult<ApiResponseDto<ImageDto>>> UploadImage([FromBody] Guid roomId ,IFormFile file)
        {
            var response = await _imageService.UploadFileAsync(roomId,file);
            return StatusCode(response.StatusCode, response);
        }

        [HttpDelete("{id}")]
        public async Task<ActionResult<ApiResponseDto<bool>>> DeleteImage(Guid id)
        {
            var response = await _imageService.DeleteFileAsync(id);
            return StatusCode(response.StatusCode, response);
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<ApiResponseDto<ImageDto>>> GetImageById(Guid id)
        {
            var response = await _imageService.GetImageByIdAsync(id);
            return StatusCode(response.StatusCode, response);
        }
    }
}
