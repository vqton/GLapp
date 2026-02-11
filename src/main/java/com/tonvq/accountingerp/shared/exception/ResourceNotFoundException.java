package com.tonvq.accountingerp.shared.exception;

/**
 * Resource Not Found Exception
 * 
 * Ném khi tìm không thấy resource
 * 
 * @author Ton VQ
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s không tìm thấy với %s = '%s'", resourceName, fieldName, fieldValue),
            "RESOURCE_NOT_FOUND"
        );
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}
