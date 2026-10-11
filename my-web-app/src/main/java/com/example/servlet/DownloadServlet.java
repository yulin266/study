package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. 定位要下载的文件：webapp/files/sample.txt 部署后会变成 /files/sample.txt
        String realPath = getServletContext().getRealPath("/files/sample.txt");
        File file = new File(realPath);

        // 文件不存在就返回 404
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        // 2. 响应头①：声明这是二进制文件，而不是网页
        resp.setContentType("application/octet-stream");

        // 3. 响应头②：attachment 让浏览器弹出"另存为"，filename 指定下载后的文件名
        //    中文文件名必须 URL 编码，否则下载时乱码
        String fileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8);
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"");

        // 4. 可选：告诉浏览器文件大小，下载进度条才准确
        resp.setContentLengthLong(file.length());

        // 5. 把文件字节通过输出流写给浏览器（8192 字节一块，边读边写）
        try (InputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
        // try-with-resources：in 和 out 用完全自动关闭
    }
}