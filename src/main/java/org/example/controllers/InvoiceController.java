package org.example.controllers;

import org.example.dto.invoice.InvoiceCreateDTO;
import org.example.exceptions.InvoiceNotFoundException;
import org.example.model.Invoice;
import org.example.service.IInvoiceService;
import org.example.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Controller
@RequestMapping("/invoice")
public class InvoiceController {

    private static final String UPLOAD_DIR = "uploading/";

    @Autowired
    private IInvoiceService service;

    @Autowired
    private StorageService storageService;

    @GetMapping("/")
    public String showHomePage() {
        return "homePage";
    }

    @GetMapping("/register")
    public String showRegistration() {
        return "registerInvoicePage";
    }

    @PostMapping("/save")
    public String saveInvoice(
            @ModelAttribute InvoiceCreateDTO dto,//  Model model,
            RedirectAttributes attributes
    ) {
        Long id = service.saveInvoice(dto).getId();
        //String message = "Record with id : '"+id+"' is saved successfully !";
//        model.addAttribute("message", message);
//        return "registerInvoicePage";

        attributes.addAttribute("message", "Record with id : '"+id+"' is saved successfully !");
        return "redirect:getAllInvoices";
    }

    @GetMapping("/getAllInvoices")
    public String getAllInvoices(
            @RequestParam(value = "message", required = false) String message,
            Model model
    ) {
        List<Invoice> invoices= service.getAllInvoices();
        model.addAttribute("list", invoices);
        model.addAttribute("message", message);
        return "allInvoicesPage";
    }

    @GetMapping("/edit")
    public String getEditPage(
            Model model,
            RedirectAttributes attributes,
            @RequestParam Long id
    ) {
        String page;
        try {
            Invoice invoice = service.getInvoiceById(id);
            InvoiceCreateDTO dto = new InvoiceCreateDTO();
            dto.setId(invoice.getId());
            dto.setName(invoice.getName());
            dto.setLocation(invoice.getLocation());
            dto.setAmount(invoice.getAmount());

            model.addAttribute("invoice", dto);
            page = "editInvoicePage";
        } catch (InvoiceNotFoundException e) {
            e.printStackTrace();
            attributes.addAttribute("message", e.getMessage());
            page = "redirect:getAllInvoices";
        }
        return page;
    }

    @PostMapping("/update")
    public String updateInvoice(
            @ModelAttribute InvoiceCreateDTO dto,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes attributes
    ) {
        try {
            // Fetch the existing invoice
            Invoice existingInvoice = service.getInvoiceById(dto.getId());

            // Update basic fields
            existingInvoice.setName(dto.getName());
            existingInvoice.setLocation(dto.getLocation());
            existingInvoice.setAmount(dto.getAmount());

            // Handle file upload
            if (file != null && !file.isEmpty()) {
                // Delete the old file if it exists
                String oldFileName = existingInvoice.getFileName();
                if (oldFileName != null && !oldFileName.isEmpty()) {
                    storageService.delete(oldFileName); // Delete the old file
                }

                // Save the new file and update the filename
                String newFileName = storageService.save(file);
                existingInvoice.setFileName(newFileName);
            }

            // Update the invoice in the database
            service.updateInvoice(existingInvoice);

            Long id = existingInvoice.getId();
            attributes.addAttribute("message", "Invoice with id: '" + id + "' is updated successfully!");
        } catch (InvoiceNotFoundException e) {
            e.printStackTrace();
            attributes.addAttribute("message", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            attributes.addAttribute("message", "Error saving file. Please try again.");
        }

        return "redirect:getAllInvoices";
    }

    @GetMapping("/delete")
    public String deleteInvoice(
            @RequestParam Long id,
            RedirectAttributes attributes
    ) {
        try {
            service.deleteInvoiceById(id);
            attributes.addAttribute("message", "Invoice with Id : '"+id+"' is removed successfully!");
        } catch (InvoiceNotFoundException e) {
            e.printStackTrace();
            attributes.addAttribute("message", e.getMessage());
        }
        return "redirect:getAllInvoices";
    }


    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filename") String filename) {
        try {
            Path file = Paths.get(storageService.getRootLocation().toString()).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
