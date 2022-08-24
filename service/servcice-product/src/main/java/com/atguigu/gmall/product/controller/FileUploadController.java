package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/product")
public class FileUploadController {
//

    @PostMapping("/fileUpload")
    public Result fileUpload(@RequestPart MultipartFile file) {
        System.out.println(file.getOriginalFilename());
        return Result.ok();
    }

}
