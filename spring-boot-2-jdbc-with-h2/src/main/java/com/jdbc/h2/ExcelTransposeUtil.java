package com.jdbc.h2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.h2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class ExcelTransposeUtil {

	
	
	public static void mainTranspose(RAWRepo repo) {
		
		FileInputStream file;
		try {
			file = new FileInputStream(new File ("C:\\Users\\KAMAL\\Downloads\\Financials_GuruFocus_2018-02-22-17-52.xls"));
	
			Workbook wb;
			wb = new HSSFWorkbook(file);
		
		
			wb = ExcelTransposeUtil.transpose(wb,0,true);
			
			file.close();
			FileOutputStream out = new FileOutputStream(new File ("C:\\Users\\KAMAL\\Downloads\\Financials_GuruFocus_2018-02-22-17-52MOD2.xls"));
			wb.write(out);
			out.close();
			
			
			
			
			ToCSV csvObj = new ToCSV();
			csvObj.convertExcelToCSV("C:\\Users\\KAMAL\\Downloads\\Financials_GuruFocus_2018-02-22-17-52MOD2.xls", "C:\\Users\\KAMAL\\Downloads\\");
			
			ExcelTransposeUtil util = new ExcelTransposeUtil();
			util.insert(wb, repo);
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void insert(Workbook wb, RAWRepo repo) {
		Sheet sheet = wb.getSheetAt(0);
	    int noCol = detectMergedCells( sheet);
	    Pair<Integer, Integer> lastRowColumn = getLastRowAndLastColumn(sheet);
	    int lastRow = lastRowColumn.getFirst();
	    int lastColumn = lastRowColumn.getSecond();

	   // RawPOJO pojo =new RawPOJO();
        String methodName = "";
        List<RawPOJO> list = new ArrayList<RawPOJO>();
        Row header = sheet.getRow(0);
	    for (int rowNum = 1; rowNum <= lastRow; rowNum++) {
	    	RawPOJO pojo =new RawPOJO();
	        Row row = sheet.getRow(rowNum);
	        if (row == null) {
	            continue;
	        }
	        for (int columnNum = 0; columnNum < lastColumn; columnNum++) {
	            Cell cell = row.getCell(columnNum);
	            
	            try { 
	            	//if(null==header.getCell(cellnum))
	            	methodName = "set"+StringUtils.toUpperEnglish(header.getCell(columnNum).getStringCellValue());
	            	
	            	Method m = pojo.getClass().getMethod(methodName, String.class);
	            	if(null!=cell && null!=cell.getStringCellValue())
	            		m.invoke(pojo, cell.getStringCellValue());
					
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        pojo.setIds(rowNum);
	        repo.save(pojo);
	       // list.add(pojo);
	    }
	    //repo.save(list);
	}
	
	
	public static Workbook transpose(Workbook wb, int sheetNum, boolean replaceOriginalSheet) {
	    Sheet sheet = wb.getSheetAt(sheetNum);
	    int noCol = detectMergedCells( sheet);
	    Pair<Integer, Integer> lastRowColumn = getLastRowAndLastColumn(sheet);
	    int lastRow = lastRowColumn.getFirst();
	    int lastColumn = lastRowColumn.getSecond();

	    System.out.println("Sheet {} has {} rows and {} columns, transposing ..."+ new Object[] {sheet.getSheetName(), 1+lastRow, lastColumn});

	    List<CellModel> allCells = new ArrayList<CellModel>();
	    for (int rowNum = 20; rowNum <= lastRow; rowNum++) {
	        Row row = sheet.getRow(rowNum);
	        if (row == null) {
	            continue;
	        }
	        for (int columnNum = 0; columnNum < lastColumn; columnNum++) {
	            Cell cell = row.getCell(columnNum);
	            allCells.add(new CellModel(cell));
	        }
	    }
	    System.out.println("Read {} cells ... transposing them"+allCells.size());

	    Sheet tSheet = wb.createSheet(sheet.getSheetName() + "_transposed");
	    for (CellModel cm : allCells) {
	        if (cm.isBlank()) {
	            continue;
	        }

	        int tRow = cm.getColNum();
	        int tColumn = cm.getRowNum()-20;

	        Row row = tSheet.getRow(tRow);
	        if (row == null) {
	            row = tSheet.createRow(tRow);
	        }

	        Cell cell = row.createCell(tColumn);
	        cm.insertInto(cell);
	    }

	    lastRowColumn = getLastRowAndLastColumn(sheet);
	    lastRow = lastRowColumn.getFirst();
	    lastColumn = lastRowColumn.getSecond();
	    
	    insertMoreCol(tSheet,noCol);
	    
	    System.out.println("Transposing done. {} now has {} rows and {} columns."+ new Object[] {tSheet.getSheetName(), 1+lastRow, lastColumn});
	    if (replaceOriginalSheet) {
	        int pos = wb.getSheetIndex(sheet);
	        wb.removeSheetAt(pos);
	        wb.setSheetOrder(tSheet.getSheetName(), pos);
	    }
	    return wb;
	}

	private static void insertMoreCol(Sheet sheet,int noCol) {
		 Pair<Integer, Integer> lastRowColumn = getLastRowAndLastColumn(sheet);
		 int lastRow = lastRowColumn.getFirst();
		 int lastColumn = lastRowColumn.getSecond();
		for (int rowNum = 0; rowNum <= lastRow; rowNum++) {
	        Row row = sheet.getRow(rowNum);
	        if (row == null) {
	            continue;
	        }else if (rowNum == 0) {
	        	row.createCell(lastColumn).setCellValue("Report");
	        	row.createCell(lastColumn+1).setCellValue("Stock");
	        	continue;
	        }
	            Cell cell = row.createCell(lastColumn);
	            if(rowNum<noCol)
	            	cell.setCellValue("A");
	            else
	            	cell.setCellValue("Q");
	            
	            Cell cell1 = row.createCell(lastColumn+1);
	            	cell1.setCellValue(sheet.getSheetName().replace("_transposed", ""));
	    }
	}
	private static int detectMergedCells(Sheet sheet) {
		
		CellRangeAddress mRegion = null;
		
		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			
			mRegion = sheet.getMergedRegion(i);
			System.out.println(mRegion.toString());
			int fromRow = mRegion.getFirstRow();
			int toRow = mRegion.getLastRow();
			int fromCol = mRegion.getFirstColumn();
			int toCol = mRegion.getLastColumn();
			int noC =	mRegion.getNumberOfCells();
			System.out.println(noC);
			Row row = sheet.getRow(fromRow);
			Cell cell = row.getCell(fromCol);
			System.out.println("cell"+cell.toString());
			if(cell.toString().equals("Annual Data"))
				return noC;
			//Do something here with the infos
		}
		return 0;
	}
	private static Pair<Integer, Integer> getLastRowAndLastColumn(Sheet sheet) {
	    int lastRow = sheet.getLastRowNum();
	    int lastColumn = 0;
	    for (Row row : sheet) {
	        if (lastColumn < row.getLastCellNum()) {
	            lastColumn = row.getLastCellNum();
	        }
	    }
	    return new Pair<Integer, Integer>(lastRow, lastColumn);
	}
	
	static class CellModel {
	    private int rowNum = -1;
	    private int colNum = -1;
	    private CellStyle cellStyle;
	    private int cellType = -1;
	    private Object cellValue;

	    public CellModel(Cell cell) {
	        if (cell != null) {
	            this.rowNum = cell.getRowIndex();
	            this.colNum = cell.getColumnIndex();
	            this.cellStyle = cell.getCellStyle();
	            this.cellType = cell.getCellType();
	            switch (this.cellType) {
	                case Cell.CELL_TYPE_BLANK:
	                    break;
	                case Cell.CELL_TYPE_BOOLEAN:
	                    cellValue = cell.getBooleanCellValue();
	                    break;
	                case Cell.CELL_TYPE_ERROR:
	                    cellValue = cell.getErrorCellValue();
	                    break;
	                case Cell.CELL_TYPE_FORMULA:
	                    cellValue = cell.getCellFormula();
	                    break;
	                case Cell.CELL_TYPE_NUMERIC:
	                    cellValue = String.valueOf(cell.getNumericCellValue());
	                    break;
	                case Cell.CELL_TYPE_STRING:
	                    cellValue = cell.getRichStringCellValue();
	                    break;
	            }
	        }
	    }

	    public boolean isBlank() {
	        return this.cellType == -1 && this.rowNum == -1 && this.colNum == -1;
	    }

	    public void insertInto(Cell cell) {
	        if (isBlank()) {
	            return;
	        }

	        cell.setCellStyle(this.cellStyle);
	        cell.setCellType(this.cellType);
	        if(this.cellType!=Cell.CELL_TYPE_STRING)
	        	System.out.println("Cell.CELL_TYPE_STRING"+this.cellType);
	        
	        switch (this.cellType) {
	            case Cell.CELL_TYPE_BLANK:
	                break;
	            case Cell.CELL_TYPE_BOOLEAN:
	                cell.setCellValue((boolean) this.cellValue);
	                break;
	            case Cell.CELL_TYPE_ERROR:
	                cell.setCellErrorValue((byte) this.cellValue);
	                break;
	            case Cell.CELL_TYPE_FORMULA:
	                cell.setCellFormula((String) this.cellValue);
	                break;
	            case Cell.CELL_TYPE_NUMERIC:
	                cell.setCellValue(String.valueOf(this.cellValue));
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	String field = formatString(((RichTextString) this.cellValue).getString()); 
	            	
	                cell.setCellValue(field);
	                break;
	        }
	    }

	    public String formatString(String field) {
	    	field = field.replaceAll("[^a-zA-Z0-9]", "");
        	if(field.equals("5YearEBITDAGrowthRate"))
        		field = "FiveYearEBITDAGrowthRate";
	        return field;
	    }
	    
	    public CellStyle getCellStyle() {
	        return cellStyle;
	    }

	    public int getCellType() {
	        return cellType;
	    }

	    public Object getCellValue() {
	        return cellValue;
	    }

	    public int getRowNum() {
	        return rowNum;
	    }

	    public int getColNum() {
	        return colNum;
	    }

	}
}