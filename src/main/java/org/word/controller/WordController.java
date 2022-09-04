package org.word.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.word.config.SwaggerCustomProperties;
import org.word.service.WordService;
import org.word.utils.ConstantUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Controller
@Api(tags = "the toWord API")
@Slf4j
public class WordController {

    @Resource
    private SwaggerCustomProperties properties;

    @Resource
    private WordService tableService;

    @Resource
    private SpringTemplateEngine springTemplateEngine;

    /**
     * 将 swagger 文档转换成 html 文档，可通过在网页上右键另存为 xxx.doc 的方式转换为 word 文档
     *
     * @param model
     * @param url   需要转换成 word 文档的资源地址
     * @return
     */
    @ApiResponses(value = {@ApiResponse(code = 200, message = "请求成功。", response = String.class)})
    @GetMapping(value = "/word-page")
    public String toWordPage(Model model,
                             @ApiParam(value = "资源地址") @RequestParam(value = "url", required = false) String url,
                             @ApiParam(value = "是否下载") @RequestParam(value = "showDownload", required = false, defaultValue = "1") Integer showDownload) {
        generateModelData(model, url, showDownload);
        return ConstantUtil.WORD;
    }


    /**
     * 将 swagger 文档一键下载为 doc 文档
     *
     * @param model
     * @param url      需要转换成 word 文档的资源地址
     * @param response
     */
    @ApiOperation(value = "将 swagger 文档一键下载为 doc 文档", notes = "", tags = {"Word"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "请求成功。")})
    @GetMapping(value = "/fetch-word-use-url")
    public void downloadWordUseUrl(Model model, @ApiParam(value = "资源地址") @RequestParam(required = false) String url, HttpServletResponse response) {
        generateModelData(model, url, 0);
        writeContentToResponse(model, response);
    }

    /**
     * 将 swagger json文件转换成 word文档并下载
     *
     * @param model
     * @param jsonFile 需要转换成 word 文档的swagger json文件
     * @param response
     * @return
     */
    @ApiOperation(value = "将 swagger json文件转换成 word文档并下载", notes = "", tags = {"Word"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "请求成功。")})
    @PostMapping(value = "/fetch-word-use-file")
    public void downloadWordUseFile(Model model, @ApiParam("swagger json file") @Valid @RequestPart("jsonFile") MultipartFile jsonFile, HttpServletResponse response) {
        generateModelData(model, jsonFile);
        writeContentToResponse(model, response);
    }

    /**
     * 将 swagger json字符串转换成 word文档并下载
     *
     * @param model
     * @param jsonStr  需要转换成 word 文档的swagger json字符串
     * @param response
     * @return
     */
    @ApiOperation(value = "将 swagger json字符串转换成 word文档并下载", notes = "", tags = {"Word"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "请求成功。")})
    @PostMapping(value = "/fetch-word-use-str")
    public void downloadWordUseStr(Model model, @ApiParam("swagger json string") @Valid @RequestParam("jsonStr") String jsonStr, HttpServletResponse response) {
        generateModelData(model, jsonStr);
        writeContentToResponse(model, response);
    }

    private void generateModelData(Model model, String url, Integer download) {
        url = StringUtils.defaultIfBlank(url, this.properties.getUrl());
        Map<String, Object> result = this.tableService.tableList(url);
        model.addAttribute(ConstantUtil.URL, url);
        model.addAttribute(ConstantUtil.SHOW_DOWNLOAD, download);
        model.addAllAttributes(result);
    }

    private void generateModelData(Model model, String jsonStr) {
        Map<String, Object> result = this.tableService.tableListFromString(jsonStr);
        model.addAttribute(ConstantUtil.URL, ConstantUtil.HTTP_HEAD);
        model.addAttribute(ConstantUtil.SHOW_DOWNLOAD, 0);
        model.addAllAttributes(result);
    }

    private void generateModelData(Model model, MultipartFile jsonFile) {
        Map<String, Object> result = this.tableService.tableList(jsonFile);
        model.addAttribute(ConstantUtil.URL, ConstantUtil.HTTP_HEAD);
        model.addAttribute(ConstantUtil.SHOW_DOWNLOAD, 0);
        model.addAllAttributes(result);
    }

    private void writeContentToResponse(Model model, HttpServletResponse response) {
        Context context = new Context();
        context.setVariables(model.asMap());
        String content = this.springTemplateEngine.process(ConstantUtil.WORD, context);
        response.setContentType("application/octet-stream;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(System.currentTimeMillis() + ".doc", "utf-8"));
            byte[] bytes = content.getBytes();
            bos.write(bytes, 0, bytes.length);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("writeContentToResponse exception:{}", e.getLocalizedMessage(), e);
        }
    }
}
