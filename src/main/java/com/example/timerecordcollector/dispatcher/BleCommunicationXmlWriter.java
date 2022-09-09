package com.example.timerecordcollector.dispatcher;


import com.example.timerecordcollector.bean.BleCommunicationSummary;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;



public class BleCommunicationXmlWriter {
    protected File file;
    protected OutputStream os;
    protected Workbook book = null;
    public BleCommunicationXmlWriter() {
        super();
    }

    private String sheetName = "sheet1";

    public BleCommunicationXmlWriter(File file) throws IOException, InvalidFormatException {
        super();
        this.file = file;
        if(!file.exists()) {
            file.createNewFile();
        }
        os = new FileOutputStream(file);
        book = new XSSFWorkbook();
        Sheet sheet = book.createSheet(sheetName);

        String[] title = {"app扫描时间", "app连接时间", "sniffer连接时间", "app服务发现时间", "sniffer服务发现时间", "app读写数据时间", "sniffer读写数据时间"};
        Row titleRow = sheet.createRow(0);
        for(int i = 0; i < title.length; i++) {
            Cell cell = titleRow.createCell(i + 1);
            cell.setCellValue(title[i]);
        }
    }

    public void Write(BleCommunicationSummary summary) {
        Sheet sheet = book.getSheet(sheetName);
        int lastRowNum = sheet.getLastRowNum();
        Row currentRow = sheet.createRow(lastRowNum + 1);
        currentRow.createCell(0).setCellFormula("ROW() - 1");
        currentRow.createCell(1).setCellValue(summary.getAppSideFindDevice());
        currentRow.createCell(2).setCellValue(summary.getAppSideEstablishConnection());
        currentRow.createCell(3).setCellValue(summary.getSnifferSideEstablishConnection());
        currentRow.createCell(4).setCellValue(summary.getAppSideServiceDiscovery());
        currentRow.createCell(5).setCellValue(summary.getSnifferSideServiceDiscovery());
        currentRow.createCell(6).setCellValue(summary.getAppSideInfoExchange());
        currentRow.createCell(7).setCellValue(summary.getSnifferSideInfoExchange());
    }

    public void Write(ArrayList<BleCommunicationSummary> bleTimeRecords) {
        for(BleCommunicationSummary u : bleTimeRecords) {
            this.Write(u);
        }
    }


    public void Extract() throws IOException {
        book.write(os);
        book.close();
        os.close();
    }
}