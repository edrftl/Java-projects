package org.example.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCreateDTO {
    private Long id;
    private String name;
    private String location;
    private MultipartFile file;
    private Double amount;
}