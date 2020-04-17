package com.nucleus.web.dynamicOfflineTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.nucleus.logging.BaseLoggers;

/**
 * 
 * @author Nucleus Software Exports Limited Dynamic Form Service Implementation
 */
@Named(value = "templateDownloadEngine")
public class TemplateDownloadEngine {

    public void processContent(String dynamicHtmlContent, String fileUrl, String hostUrl, String formName,
            String formVersion, CloseableHttpClient client) {
        String appendLabel = " <link href='/finnone-webapp/resource-bundles/JS_Messages/FileName.properties' type='text'>";
        int startIndex = appendLabel.indexOf("FileName");
        String locale = Locale.getDefault().toString();
        Document document = Jsoup.parse(dynamicHtmlContent);
        List<String> offlineLabelList = OfflineLabelsFiles.offlineLablesList;
        for (String offlineLables : offlineLabelList) {
            StringBuffer appendLabelUrl = new StringBuffer(appendLabel);
            /*if (locale.equals("en_IN")) {
                appendLabelUrl.replace(startIndex, startIndex + 8, offlineLables);
            } else {
                appendLabelUrl.replace(startIndex, startIndex + 8, offlineLables + "_" + locale);
            }*/

            // TODO: Add locale specific handling functionality
            appendLabelUrl.replace(startIndex, startIndex + 8, offlineLables);
            document.append(appendLabelUrl.toString());
        }
        Elements linksList = document.select("link[href]");
        Elements scriptsList = document.select("script[src]");
        Elements hrefList = document.select("a.FILinks");
        Elements imageList = document.select("img[src]");
        if (CollectionUtils.isNotEmpty(linksList)) {
            for (Element link : linksList) {
                String linkHref = link.attr("href");
                linkHref = hostUrl + linkHref;
                link.attr("href", linkHref);
            }
        }

        if (CollectionUtils.isNotEmpty(scriptsList)) {
            for (Element script : scriptsList) {
                String src = script.attr("src");
                src = hostUrl + src;
                script.attr("src", src);
            }
        }
        if (CollectionUtils.isNotEmpty(imageList)) {
            for (Element link : imageList) {
                String linkHref = link.attr("src");
                linkHref = hostUrl + linkHref;
                link.attr("src", linkHref);
            }
        }
        if (CollectionUtils.isNotEmpty(linksList)) {
            CloseableHttpResponse httpResponse = null;
            for (Element link : linksList) {
                try {
                    URI uri = new URIBuilder(link.attr("href")).build();
                    HttpGet httpGet = new HttpGet(uri);
                    httpResponse = client.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        BaseLoggers.flowLogger.error("Method failed: " + httpResponse.getStatusLine());
                    }

                    // Read the response body.
                    HttpEntity httpEntity = null;

                    httpEntity = httpResponse.getEntity();

                    InputStream content = httpEntity.getContent();
                    String fileName = fileUrl + "/FormConfiguration/" + link.attr("href").replace(hostUrl, "");
                    File file = new File(fileName);
                    FileUtils.write(file, IOUtils.toString(content));
                } catch (MalformedURLException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                } catch (IOException e) {
                    BaseLoggers.exceptionLogger.error("Fatal transport error: " + e.getMessage());
                } catch (URISyntaxException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                } finally {
                    try {
                        httpResponse.close();
                    } catch (IOException e) {
                        BaseLoggers.exceptionLogger.error(e.getMessage());
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(scriptsList)) {
            CloseableHttpResponse httpResponse = null;
            for (Element script : scriptsList) {

                try {
                    URI uri = new URIBuilder(script.attr("src")).build();
                    HttpGet httpGet = new HttpGet(uri);
                    httpResponse = client.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        BaseLoggers.flowLogger.error("Method failed: " + httpResponse.getStatusLine());
                    }

                    // Read the response body.
                    HttpEntity httpEntity = null;
                    httpEntity = httpResponse.getEntity();
                    InputStream content = httpEntity.getContent();
                    String fileName = fileUrl + "/FormConfiguration/" + script.attr("src").replace(hostUrl, "");
                    File file = new File(fileName);
                    FileUtils.write(file, IOUtils.toString(content));
                } catch (MalformedURLException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                } catch (IOException e) {
                    BaseLoggers.exceptionLogger.error("Fatal transport error: " + e.getMessage());
                } catch (URISyntaxException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                } finally {
                    try {
                        httpResponse.close();
                    } catch (IOException e) {
                        BaseLoggers.exceptionLogger.error(e.getMessage());
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(imageList)) {
            CloseableHttpResponse httpResponse = null;
            for (Element image : imageList) {

                try {
                    URI uri = new URIBuilder(image.attr("src")).build();
                    HttpGet httpGet = new HttpGet(uri);
                    httpResponse = client.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        BaseLoggers.flowLogger.error("Method failed: " + httpResponse.getStatusLine());
                    }

                    // Read the response body.
                    HttpEntity httpEntity = null;
                    httpEntity = httpResponse.getEntity();
                    InputStream content = httpEntity.getContent();
                    String fileName = fileUrl + "/FormConfiguration/" + image.attr("src").replace(hostUrl, "");
                    File file = new File(fileName);
                    FileUtils.write(file, IOUtils.toString(content));
                } catch (MalformedURLException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                } catch (IOException e) {
                    BaseLoggers.exceptionLogger.error("Fatal transport error: " + e.getMessage());
                } catch (URISyntaxException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                } finally {
                    try {
                        httpResponse.close();
                    } catch (IOException e) {
                        BaseLoggers.exceptionLogger.error(e.getMessage());
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(hrefList)) {
            CloseableHttpResponse httpResponse = null;
            for (Element link : hrefList) {

                try {
                    URI uri = new URIBuilder(link.attr("href")).build();
                    HttpPost httpPost = new HttpPost(uri);
                    httpResponse = client.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        BaseLoggers.flowLogger.error("Method failed: " + httpResponse.getStatusLine());
                    }

                    // Read the response body.
                    HttpEntity httpEntity = null;
                    httpEntity = httpResponse.getEntity();
                    InputStream content = httpEntity.getContent();
                    String htmlContent = IOUtils.toString(content);
                    Document fiDocument = Jsoup.parse(htmlContent);
                    Elements fiLinks = fiDocument.select("link[href]");
                    Elements fiScripts = fiDocument.select("script[src]");
                    for (Element links : fiLinks) {
                        String linkHref = links.attr("href");
                        linkHref = linkHref.replaceFirst("/", "");
                        links.attr("href", linkHref);
                    }
                    for (Element script : fiScripts) {
                        String src = script.attr("src");
                        src = src.replaceFirst("/", "");
                        script.attr("src", src);
                    }
                    String htmlCode = fiDocument.toString();
                    String filename = link.attr("href").replace(hostUrl, "");
                    filename = fileUrl + "/FormConfiguration/"
                            + filename.substring(filename.indexOf("=") + 1, filename.indexOf("&"));
                    filename = filename.replace("%20", " ");
                    File file = new File(filename + ".htm");
                    FileUtils.write(file, htmlCode);
                } catch (MalformedURLException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                } catch (IOException e) {
                    BaseLoggers.exceptionLogger.error("Fatal transport error: " + e.getMessage());
                } catch (URISyntaxException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                } finally {
                    try {
                        httpResponse.close();
                    } catch (IOException e) {
                        BaseLoggers.exceptionLogger.error(e.getMessage());
                    }
                }
            }
        }
        Document documentNew = Jsoup.parse(dynamicHtmlContent);

        for (String offlineLables : offlineLabelList) {
            StringBuffer appendLabelUrl = new StringBuffer(appendLabel);
            if (locale.equals("en_IN")) {
                appendLabelUrl.replace(startIndex, startIndex + 8, offlineLables);
            } else {
                appendLabelUrl.replace(startIndex, startIndex + 8, offlineLables + "_" + locale);
            }

            documentNew.append(appendLabelUrl.toString());
        }
        linksList = documentNew.select("link[href]");
        scriptsList = documentNew.select("script[src]");
        imageList = documentNew.select("img[src]");
        for (Element link : linksList) {
            String linkHref = link.attr("href");
            linkHref = linkHref.replaceFirst("/", "FormConfiguration/");
            link.attr("href", linkHref);
        }
        for (Element script : scriptsList) {
            String src = script.attr("src");
            src = src.replaceFirst("/", "FormConfiguration/");
            script.attr("src", src);
        }
        for (Element script : imageList) {
            String src = script.attr("src");
            src = src.replaceFirst("/", "FormConfiguration/");
            script.attr("src", src);
        }

        String htmlCode = documentNew.toString();
        String fileName = null;
        if (formVersion.isEmpty()) {
            fileName = fileUrl + "/" + formName + ".htm";
        } else {
            fileName = fileUrl + "/" + formName + "_" + formVersion + ".htm";
        }
        File file = new File(fileName);
        try {
            FileUtils.write(file, htmlCode);
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error(e.getMessage());
        }
        List<String> fileList = new ArrayList<String>();
        generateFileList(fileUrl, new File(fileUrl), fileList);
        zipIt(fileUrl, fileUrl + ".zip", fileList);
    }

    /**
     * Zip it
     * 
     * @param zipFile
     *            output ZIP file location
     * @param fileList
     */
    public void zipIt(String sourceFolder, String zipFile, List<String> fileList) {

        byte[] buffer = new byte[1024];
        FileOutputStream fos=null;
        FileInputStream in=null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            for (String file : fileList) {

                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);
                try {
                	in = new FileInputStream(sourceFolder + File.separator + file);

                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                	IOUtils.closeQuietly(in);
                }
            }

            zos.closeEntry();
            // remember close it
            BaseLoggers.flowLogger.debug("Done");
        } catch (IOException ex) {
            BaseLoggers.exceptionLogger.error(ex.getMessage(),ex);
        } finally {
        	IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(zos);
        }

    }

    /**
     * Traverse a directory and get all files, and add the file into fileList
     * 
     * @param node
     *            file or directory
     * @param fileList
     */
    public void generateFileList(String sourceFolder, File node, List<String> fileList) {

        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(sourceFolder, node.getAbsoluteFile().toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(sourceFolder, new File(node, filename), fileList);
            }
        }

    }

    /**
     * Format the file path for zip
     * 
     * @param file
     *            file path
     * @return Formatted file path
     */
    private String generateZipEntry(String sourceFolder, String file) {
        if (sourceFolder.contains("\\/")) {
            sourceFolder = sourceFolder.replace("\\/", "/");
        }
        BaseLoggers.flowLogger.debug("*sourceFolder*" + sourceFolder);
        BaseLoggers.flowLogger.debug("*file*" + file);
        return file.substring(sourceFolder.length() + 1, file.length());
    }
}
