package org.example.Controller;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import com.google.protobuf.ByteString;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
//import org.apache.tika.parser.mp3.LyricsHandler;
//import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AIEngine {
    private String command;
    private String text;
    private String filePath;
    private String fileType;

    public static void main(String[] args) {
        AIEngine aiEngine = new AIEngine();
        File file = new File("/Users/lamar/Downloads/video5061922730.mp4");
        aiEngine.setFilePath(file.getAbsolutePath());
        aiEngine.setFileType("mp4");
        aiEngine.setCommand("Explain the contents of this video");
        System.out.println(aiEngine.getSummary());
    }

    public AIEngine(){
        this.command="";
        this.text="";
        this.filePath="";
        this.fileType="";
    }
    public AIEngine(String command, String text, String filePath, String fileType) {
        this.command = command;
        this.text = text;
        this.filePath = filePath;
        this.fileType = fileType;
    }

    public String getSummary(){
        System.out.println("File Path: " + filePath);
        System.out.println("File Type: " + fileType);
        System.out.println("Command: " + command);
        System.out.println("Text: " + text);
        String aiSummary="";
        try (VertexAI vertexAi = new VertexAI("atlasbrowser", "us-central1"); ) {
            GenerationConfig generationConfig =
                    GenerationConfig.newBuilder()
                            .setMaxOutputTokens(4192)
                            .setTemperature(1F)
                            .setTopP(0.95F)
                            .build();
            List<SafetySetting> safetySettings = Arrays.asList(
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                            .build()
            );
            GenerativeModel model =
                    new GenerativeModel.Builder()
                            .setModelName("gemini-1.5-flash-002")
                            .setVertexAi(vertexAi)
                            .setGenerationConfig(generationConfig)
                            .setSafetySettings(safetySettings)
                            .build();

            switch (this.fileType) {
                case "image" -> {
                    File newFile = new File(this.filePath);
                    byte[] document1Bytes = new byte[(int) newFile.length()];
                    try(FileInputStream image1FileInputStream = new FileInputStream(newFile)) {
                        image1FileInputStream.read(document1Bytes);
                    }
                    var document1 = PartMaker.fromMimeTypeAndData(
                            "image/png", document1Bytes);
                    var content = ContentMaker.fromMultiModalData(command, document1);
                    GenerateContentResponse response = model.generateContent(content);
                    if (response.getCandidatesCount() > 0) {
                        for (Candidate part : response.getCandidatesList()) {
                            aiSummary = part.getContent().getParts(0).getText().replaceAll("\n", "");
                        }
                    }
                }
                case "mp4" -> {
                    File newFile = new File(this.filePath);
                    byte[] document1Bytes = new byte[(int) newFile.length()];
                    try(FileInputStream image1FileInputStream = new FileInputStream(newFile)) {
                        image1FileInputStream.read(document1Bytes);
                    }
                    var document1 = PartMaker.fromMimeTypeAndData(
                            "video/mp4", document1Bytes);
                    var content = ContentMaker.fromMultiModalData(command, document1);
                    GenerateContentResponse response = model.generateContent(content);
                    if (response.getCandidatesCount() > 0) {
                        for (Candidate part : response.getCandidatesList()) {
                            aiSummary = part.getContent().getParts(0).getText().replaceAll("\n", "");
                        }
                    }
                }
                case "mp3" -> {
                    File newFile = new File(this.filePath);
                    byte[] document1Bytes = new byte[(int) newFile.length()];
                    try(FileInputStream image1FileInputStream = new FileInputStream(newFile)) {
                        image1FileInputStream.read(document1Bytes);
                    }
                    var document1 = PartMaker.fromMimeTypeAndData(
                            "audio/mpeg", document1Bytes);
                    var content = ContentMaker.fromMultiModalData(command, document1);
                    GenerateContentResponse response = model.generateContent(content);
                    if (response.getCandidatesCount() > 0) {
                        for (Candidate part : response.getCandidatesList()) {
                            aiSummary = part.getContent().getParts(0).getText().replaceAll("\n", "");
                        }
                    }
                }
                case null, default -> {
                    this.text = parseToPlainText(this.filePath);
                    var content = ContentMaker.fromMultiModalData(this.command + this.text);
                    GenerateContentResponse response = model.generateContent(content);
                    if (response.getCandidatesCount() > 0) {
                        for (Candidate part : response.getCandidatesList()) {
                            aiSummary = part.getContent().getParts(0).getText().replaceAll("\n", "");
                        }
                    }
                }
            }
        } catch (IOException | SAXException | TikaException e) {
            e.printStackTrace();
        }
        return aiSummary;
    }

    public String parseToPlainText(String fileName) throws IOException, TikaException, SAXException
    {
//        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(fileName);

        FileInputStream stream = new FileInputStream(new File(fileName));
        Parser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        System.out.println(handler.toString().length());

        // Parsing the string
        parser.parse(stream, handler, metadata, context);

        //|0-9 9-18|     18-27 27-36 36-45     |45-54 54-63|     63-72 72-81 81-90          |90-99 99-108|
        return "Starts with: " + handler.toString().substring(0, Math.round((float) ((handler.toString().length() /12)*2)))
                +"Middle Content:"+ handler.toString().substring(Math.round((float)(handler.toString().length()/12)*5), Math.round((float)(handler.toString().length()/12)*7))
                +"Ending Content:" + handler.toString().substring((handler.toString().length()/12)*10, handler.toString().length()-1);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
