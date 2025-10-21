package response

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// APIResponse represents the standard API response format
type APIResponse struct {
	IsSuccess  bool        `json:"isSuccess" example:"true"`
	Data       interface{} `json:"data,omitempty"`
	Message    string      `json:"message" example:"Success"`
	StatusCode int         `json:"statusCode" example:"200"`
}

// SuccessResponse creates a successful response
func SuccessResponse(c *gin.Context, statusCode int, data interface{}, message string) {
	if message == "" {
		message = "Success"
	}

	response := APIResponse{
		IsSuccess:  true,
		Data:       data,
		Message:    message,
		StatusCode: statusCode,
	}

	c.JSON(statusCode, response)
}

// ErrorResponse creates an error response
func ErrorResponse(c *gin.Context, statusCode int, message string) {
	response := APIResponse{
		IsSuccess:  false,
		Data:       nil,
		Message:    message,
		StatusCode: statusCode,
	}

	c.JSON(statusCode, response)
}

// ValidationErrorResponse creates a validation error response
func ValidationErrorResponse(c *gin.Context, message string) {
	ErrorResponse(c, http.StatusBadRequest, message)
}

// InternalErrorResponse creates an internal server error response
func InternalErrorResponse(c *gin.Context, message string) {
	if message == "" {
		message = "Internal server error"
	}
	ErrorResponse(c, http.StatusInternalServerError, message)
}

// NotFoundResponse creates a not found error response
func NotFoundResponse(c *gin.Context, message string) {
	if message == "" {
		message = "Resource not found"
	}
	ErrorResponse(c, http.StatusNotFound, message)
}