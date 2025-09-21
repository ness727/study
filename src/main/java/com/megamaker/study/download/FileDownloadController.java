package com.megamaker.study.download;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@RestController
public class FileDownloadController {

    @GetMapping("/v1/file")
    public void downloadFileV1(HttpServletResponse response) {
        String fileName = "pycharm-2025.1.2.exe";
        File file = new File("D:\\" + fileName);

        response.setContentType("application/vnd.microsoft.portable-executable");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLengthLong(file.length());

        int data;
        try (FileInputStream in = new FileInputStream(file);
             ServletOutputStream out = response.getOutputStream();
        ) {
            while ((data = in.read()) != -1) {
                out.write(data);
            }
            out.flush();
        } catch (IOException e) {
            log.error("파일 다운로드 실패", e);
        }
    }

    @GetMapping("/v2/file")
    public void downloadFileV2(HttpServletResponse response) {
        String fileName = "pycharm-2025.1.2.exe";
        File file = new File("D:\\" + fileName);

        response.setContentType("application/vnd.microsoft.portable-executable");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLengthLong(file.length());

        byte[] buffer = new byte[8192];
        try (FileInputStream in = new FileInputStream(file);
             ServletOutputStream out = response.getOutputStream();
        ) {
            while (in.read(buffer) != -1) {
                out.write(buffer, 0, buffer.length);
            }
            out.flush();
        } catch (IOException e) {
            log.error("파일 다운로드 실패", e);
        }
    }

    @GetMapping("/v3/file")
    public void downloadFileV3(HttpServletResponse response) throws IOException {
        String fileName = "pycharm-2025.1.2.exe";
        Path path = Paths.get("D:\\" + fileName);

        response.setContentType("application/vnd.microsoft.portable-executable");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLengthLong(Files.size(path));

        try (FileInputStream in = new FileInputStream(path.toFile());
             ServletOutputStream out = response.getOutputStream();
        ) {
            Files.copy(path, out);
            out.flush();
        } catch (IOException e) {
            log.error("파일 다운로드 실패", e);
        }
    }

    @GetMapping("/v4/file")
    public void downloadFileV4(HttpServletResponse response) throws IOException {
        String fileName = "pycharm-2025.1.2.exe";
        Path path = Paths.get("D:\\" + fileName);

        response.setContentType("application/vnd.microsoft.portable-executable");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + UriUtils.encode(fileName, StandardCharsets.UTF_8) + "\"");
        response.setContentLengthLong(Files.size(path));

        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
             WritableByteChannel outChannel = Channels.newChannel(response.getOutputStream());
        ) {
            fileChannel.transferTo(0, fileChannel.size(), outChannel);
        } catch (IOException e) {
            log.error("파일 다운로드 실패", e);
        }
    }


    @GetMapping("/v5/file")
    public ResponseEntity<UrlResource> downloadFileV5(HttpServletResponse response) throws IOException {
        String fileName = "pycharm-2025.1.2.exe";
        Path path = Paths.get("D:", fileName);
        UrlResource urlResource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + UriUtils.encode(fileName, StandardCharsets.UTF_8) + "\"")
                .body(urlResource);
    }
}
