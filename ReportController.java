package arris.ps.controller;

import arris.ps.dto.RestoreCallDto;
import arris.ps.modelOracle.ActionList;
import arris.ps.repositoryOracle.ReportRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by rkaur on 3/2/2017.
 */
@Controller
@EnableAutoConfiguration
public class ReportController {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    protected final static org.slf4j.Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ReportRepository reportRepository;


    @RequestMapping(value = "/actionlist")
    public String actionList(Model model,
                             @PageableDefault(sort = "type", direction = Sort.Direction.ASC, size = 100) Pageable pageable) {

        Date[] dates = getSessionDates();
        Date fromDate = dates[0];
        Date toDate = dates[1];
        //Page<ActionList> actionListPage = reportRepository.actionList(fromDate,toDate,pageable);
        List<ActionList> actionListRepo = reportRepository.actionList(fromDate,toDate);




        List<ActionList> filteredList = new ArrayList<>();

        Iterator<ActionList> iterator = actionListRepo.iterator();
        while (iterator.hasNext())
        {
            ActionList actionList = iterator.next();

            if(actionList.getCallerIP().contains("144.160.130.16")){

                actionList.setCallName("WFE");
            }
            else if(actionList.getCallerIP().contains("144.160.226.")){

                actionList.setCallName("ISAAC");
            }
            else if(actionList.getCallerIP().contains("144.160.5.")){

                actionList.setCallName("TS&R");
            }
            else if(actionList.getCallerIP().contains("64.186.")){

                actionList.setCallName("TS&R");
            }
            else
                actionList.setCallName("");

         if ( !((actionList.getBanId() == null) || (actionList.getMemberId() == null) ||
                    (actionList.getResult() == null) || (actionList.getDeviceRestoreSerial() == null) ||
                    (actionList.getDeviceModel() == null) || (actionList.getDeviceRestoreModel() == null) ||
                    (actionList.getCollectionTime() == null) || (actionList.getCallName() == null) || (actionList.getError() == null)) )


          {

                filteredList.add(actionList);
            }
        }
        int start = pageable.getOffset();
        int end = (start + pageable.getPageSize()) > filteredList.size() ? filteredList.size() : (start + pageable.getPageSize());
        Page<ActionList> actionFilteredListPage = new PageImpl<ActionList>(filteredList.subList(start,end), pageable,filteredList.size());

        if(actionFilteredListPage!=null) {
            String deviceSourceModelLocal = "";
            String deviceDestinationModelLocal = "";

            for (ActionList list : filteredList) {
                deviceSourceModelLocal = list.getDeviceModel();
                deviceDestinationModelLocal = list.getDeviceRestoreModel();
            }
            logger.info("*****************device Source Model"+deviceSourceModelLocal);
            SimpleDateFormat format1 = new SimpleDateFormat("MM/dd/yyy");
            model.addAttribute("fromDate",format1.format(fromDate) );
            model.addAttribute("toDate",format1.format(toDate));
            String  deviceSourceModel=  getDeviceDestinationAndRestoreModel(deviceSourceModelLocal);
            String deviceDestinationModel=getDeviceDestinationAndRestoreModel(deviceDestinationModelLocal);
            model.addAttribute("actionListPage", actionFilteredListPage);
            model.addAttribute("deviceDestinationModel", deviceDestinationModel);
            model.addAttribute("deviceSourceModel",deviceSourceModel);
        }
        return "reports/action-list";
    }
    /*  @RequestMapping(value = "/actionlist")
      public String actionList(Model model,
                               @PageableDefault(sort = "type", direction = Sort.Direction.ASC, size = 5) Pageable pageable) {

          Date[] dates = getSessionDates();
          Date fromDate = dates[0];
          Date toDate = dates[1];

          Page<ActionList> actionListPage = reportRepository.actionList(fromDate,toDate,pageable);
          List<ActionList> actionListRepo = actionListPage.getContent();

          List<ActionList> filteredList = new ArrayList<>();

          Iterator<ActionList> iterator = actionListRepo.iterator();
          while (iterator.hasNext())
          {
              ActionList actionList = iterator.next();

              if ( !((actionList.getBanId() == null) || (actionList.getMemberId() == null) ||
                      (actionList.getResult() == null) || (actionList.getDeviceRestoreSerial() == null) ||
                      (actionList.getDeviceModel() == null) || (actionList.getDeviceRestoreModel() == null) ||
                      (actionList.getCollectionTime() == null) || (actionList.getCallName() == null) || (actionList.getError() == null)) ){

                  filteredList.add(actionList);
              }
          }
          int start = pageable.getOffset();
          int end = (start + pageable.getPageSize()) > filteredList.size() ? filteredList.size() : (start + pageable.getPageSize());
          Page<ActionList> actionFilteredListPage = new PageImpl<ActionList>(filteredList.subList(start,end), pageable,filteredList.size());

  //logger.info("******************"+list1);
          if(actionListPage!=null) {
              String deviceSourceModelLocal = "";
              String deviceDestinationModelLocal = "";

              for (ActionList list : filteredList) {
                  deviceSourceModelLocal = list.getDeviceModel();
                  deviceDestinationModelLocal = list.getDeviceRestoreModel();
              }
  logger.info("*****************device Source Model"+deviceSourceModelLocal);
              SimpleDateFormat format1 = new SimpleDateFormat("MM/dd/yyy");
              model.addAttribute("fromDate",format1.format(fromDate) );
              model.addAttribute("toDate",format1.format(toDate));
              String  deviceSourceModel=  getDeviceDestinationAndRestoreModel(deviceSourceModelLocal);
              String deviceDestinationModel=getDeviceDestinationAndRestoreModel(deviceDestinationModelLocal);
              model.addAttribute("actionListPage", actionFilteredListPage);
              model.addAttribute("deviceDestinationModel", deviceDestinationModel);
              model.addAttribute("deviceSourceModel",deviceSourceModel);
          }
          return "reports/action-list";
      }*/
/*

    /*Action Summary report*/
    @RequestMapping(value = "/actionsummary", method = RequestMethod.GET)
    public String actionSummary(Model model,
                                @PageableDefault(sort = "type", direction = Sort.Direction.ASC, size = 5) Pageable pageable) {

        Date[] dates = getSessionDates();
        Date fromDate = dates[0];
        Date toDate = dates[1];

        long totalCount = 0;
        long successCount = 0;
        long failureCount = 0;
        long errorCount = 0;
        long timeoutCount = 0;
        long nonTimeOutCount = 0;
        List<Object[]> actionSummaryList = reportRepository.actionSummary(fromDate, toDate);
        for (Object[] list : actionSummaryList) {

            totalCount = totalCount + ((BigDecimal) list[1]).longValue();
            successCount = successCount + ((BigDecimal) list[2]).longValue();
            failureCount = failureCount + ((BigDecimal) list[3]).longValue();
            errorCount = errorCount + ((BigDecimal) list[4]).longValue();
            timeoutCount = timeoutCount + ((BigDecimal) list[5]).longValue();
            nonTimeOutCount = nonTimeOutCount + ((BigDecimal) list[6]).longValue();
        }
        SimpleDateFormat customFormat = new SimpleDateFormat("MM/dd/yyy");
        model.addAttribute("fromDate", customFormat.format(fromDate));
        model.addAttribute("toDate", customFormat.format(toDate));
        model.addAttribute("actionSummaryList", actionSummaryList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("successCount", successCount);
        model.addAttribute("failureCount", failureCount);
        model.addAttribute("errorCount", errorCount);
        model.addAttribute("timeoutCount", timeoutCount);
        model.addAttribute("nonTimeOutCount", nonTimeOutCount);
        return "reports/action-summary";
    }
    /*Restoral Calls report, It calls the private method which populates the view from DTO*/
    @RequestMapping(value = "/restorecalls", method = RequestMethod.GET)
    public String restoreCalls(Model model) {
        Date[] dates = getSessionDates();
        Date fromDate = dates[0];
        Date toDate = dates[1];
        SimpleDateFormat customFormat = new SimpleDateFormat("MM/dd/yyy");
        Object[] returnData = restoreCallDtoList();
        model.addAttribute("returnRestoreList", returnData[0]);
        model.addAttribute("timeoutRestoreCallDtos", returnData[1]);
        model.addAttribute("fromDate", customFormat.format(fromDate));
        model.addAttribute("toDate", customFormat.format(toDate));
        return "reports/restore-calls";
    }
    /*RestoreCallDto for Restoral reports*/
    private Object[] restoreCallDtoList() {
        Object[] returnData = new Object[2];
        Date[] dates = getSessionDates();
        Date fromDate = dates[0];
        Date toDate = dates[1];
        List<Object[]> restoreCallsList = reportRepository.restoreCalls(fromDate, toDate);
        Map<String, RestoreCallDto> returnRestoreList = new HashMap<>();
        if (restoreCallsList == null || restoreCallsList.size() == 0) {
            return null;
        }
        int totalTrans = 0;
        int totalErrors = 0;
        int totalFailures = 0;
        int totalSuccess = 0;

        int dslFactoryResetTotal = 0;
        int dslSwapTotal=0;
        int uverseResetTotal = 0;
        int uverseSwapTotal=0;

        for (String errorType : new String[]{"E", "F", "S"}){
            for (String type: new String[]{"DSL Factory Reset", "DSL Swap", "Uverse Factory Reset", "Uverse Swap"}){
                String key = errorType + "-" + type;
                RestoreCallDto restoreDto = new RestoreCallDto();
                restoreDto.setStepName(key);
                restoreDto.setDisplayName(type);
                returnRestoreList.put(key, restoreDto);
            }
        }

        for (Object[] cols : restoreCallsList) {
            //RestoreCallDto restoreCallDto = new RestoreCallDto();
            String result = (String) cols[0];
            String actionType = (String) cols[1];
            BigDecimal transCountDb = (BigDecimal) cols[2];

            String key = result + "-" + actionType;
            returnRestoreList.get(key).setTransCount(transCountDb.intValue());
            if (result.equals("E")) {
                totalErrors += transCountDb.intValue();
            } else if (result.equals("F")) {
                totalFailures += transCountDb.intValue();
            } else if (result.equals("S")) {
                totalSuccess += transCountDb.intValue();
            }

            if (actionType.equals("DSL Factory Reset")){
                dslFactoryResetTotal += transCountDb.intValue();
            }
            else if (actionType.equals("DSL Swap")){
                dslSwapTotal += transCountDb.intValue();
            }
            else if (actionType.equals("Uverse Factory Reset")){
                uverseResetTotal += transCountDb.intValue();
            }
            else if (actionType.equals("Uverse Swap")){
                uverseSwapTotal += transCountDb.intValue();
            }

            //returnRestoreList.put(restoreCallDto.getStepName(), restoreCallDto);
            totalTrans += transCountDb.intValue();
        }
        RestoreCallDto totalErrorsDto = new RestoreCallDto();
        totalErrorsDto.setStepName("Total Errors");
        totalErrorsDto.setDisplayName("Total Errors");
        List<RestoreCallDto> timeOutCount = retrieveTimeoutErrorsBreakup(totalTrans);
       int c = 0;
        RestoreCallDto r =  (RestoreCallDto)timeOutCount.get(0);
        c = r.getTransCount();
        totalErrors = totalErrors +  c;
        totalErrorsDto.setTransCount(totalErrors);
        returnRestoreList.put(totalErrorsDto.getStepName(), totalErrorsDto);

        RestoreCallDto totalDto = new RestoreCallDto();
        totalDto.setStepName("Restores called");
        totalDto.setDisplayName("Restores called");
        totalDto.setTransCount(totalTrans);
        returnRestoreList.put(totalDto.getStepName(), totalDto);

        RestoreCallDto totalSuccessDto = new RestoreCallDto();
        totalSuccessDto.setStepName("Success");
        totalSuccessDto.setDisplayName("Success");
        totalSuccessDto.setTransCount(totalSuccess);
        returnRestoreList.put(totalSuccessDto.getStepName(), totalSuccessDto);

        RestoreCallDto totalFailedDto = new RestoreCallDto();
        totalFailedDto.setStepName("Failed");
        totalFailedDto.setDisplayName("Failed");
        totalFailedDto.setTransCount(totalFailures);
        returnRestoreList.put(totalFailedDto.getStepName(), totalFailedDto);

        RestoreCallDto dslFactoryResetDto = new RestoreCallDto();
        dslFactoryResetDto.setStepName("T-DSL Factory Reset");
        dslFactoryResetDto.setDisplayName("DSL Factory Reset");
        dslFactoryResetDto.setTransCount(dslFactoryResetTotal);
        returnRestoreList.put(dslFactoryResetDto.getStepName(), dslFactoryResetDto);

        RestoreCallDto dslSwapDto = new RestoreCallDto();
        dslSwapDto.setStepName("T-DSL Swap");
        dslSwapDto.setDisplayName("DSL Swap");
        dslSwapDto.setTransCount(dslSwapTotal);
        returnRestoreList.put(dslSwapDto.getStepName(), dslSwapDto);

        RestoreCallDto uverseSwapDto = new RestoreCallDto();
        uverseSwapDto.setStepName("T-Uverse Factory Reset");
        uverseSwapDto.setDisplayName("Uverse Factory Reset");
        uverseSwapDto.setTransCount(uverseResetTotal);
        returnRestoreList.put(uverseSwapDto.getStepName(), uverseSwapDto);

        RestoreCallDto uverseResetDto = new RestoreCallDto();
        uverseResetDto.setStepName("T-Uverse Swap");
        uverseResetDto.setDisplayName("Uverse Swap");
        uverseResetDto.setTransCount(uverseSwapTotal);
        returnRestoreList.put(uverseResetDto.getStepName(), uverseResetDto);

        /*calculating  percentage which is count/total*/
        for (RestoreCallDto dto : returnRestoreList.values()) {
            double percX100 = ((((long) dto.getTransCount()) * ((long) 10000))) / totalTrans;
            dto.setTransPerc((double) percX100 / 100);
        }
        returnData[0] = returnRestoreList;


        retrieveTimeoutErrorsBreakup(totalTrans);

        returnData[1] = retrieveTimeoutErrorsBreakup(totalTrans);
        return returnData;
    }

    private List<RestoreCallDto> retrieveTimeoutErrorsBreakup(int totalCount) {
        List<RestoreCallDto> timeoutRestoreCallDtos = new ArrayList<>();
        Date[] dates = getSessionDates();
        Date fromDate = dates[0];
        Date toDate = dates[1];
        int totalTimeoutCount = 0;
        List<Object[]> restoreCallsFailureByDeviceModel = reportRepository.restoreCallsByDeviceModelFailure(fromDate, toDate);
        for (Object[] rows : restoreCallsFailureByDeviceModel) {
            RestoreCallDto restoreCallDto = new RestoreCallDto();
            String srcModel = (String) rows[0];
            String destModel = (String) rows[1];
            BigDecimal count = (BigDecimal) rows[2];
            totalTimeoutCount += count.intValue();
            restoreCallDto.setDisplayName(srcModel + ";" + destModel);
            restoreCallDto.setStepName(srcModel + "_" + destModel);
            restoreCallDto.setTransCount(count.intValue());
            double percX100 = ((((long) restoreCallDto.getTransCount()) * ((long) 10000))) / totalCount;
            restoreCallDto.setTransPerc((double) percX100 / 100);

            timeoutRestoreCallDtos.add(restoreCallDto);
        }
        RestoreCallDto totalTimeoutDto = new RestoreCallDto();
        totalTimeoutDto.setDisplayName("Timeout Error");
        totalTimeoutDto.setStepName("Timeout_Error");
        totalTimeoutDto.setTransCount(totalTimeoutCount);
        double percX100 = ((((long) totalTimeoutDto.getTransCount()) * ((long) 10000))) / totalCount;
        totalTimeoutDto.setTransPerc((double) percX100 / 100);
        timeoutRestoreCallDtos.add(0, totalTimeoutDto);

        return timeoutRestoreCallDtos;
    }
    /*Get Actions Graph page
    * It is calling the private method which returns the list of actions graph
    *
    * */

    @RequestMapping(value = "/actionsGraph", method = RequestMethod.GET)
    public String actionsGraph (Model model) {
        getJsonForActionsGraph(model);
        return "reports/actions-graph";
    }

    /*Using map to save the key value pair, if either source or destination is null ,
    it keeps the empty string to avoid null pointer exception
    *
    * */

    @RequestMapping(value = "/jsonGraphData", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getJsonForActionsGraph(Model model) {
        Date[] dates = getSessionDates();
        Date fromDate = dates[0];
        Date toDate = dates[1];

        List<Object[]> actionsGraphList = reportRepository.actionsGraph(fromDate, toDate);

        List<Map<String, Object>> actionGraphjsonList = new ArrayList<Map<String, Object>>();
        // setting top ten rows for graph
        int limit = 10;
        int totalRows = actionsGraphList.size() < limit? actionsGraphList.size(): limit;
        for (int i=0; i<totalRows; i++){
            {
                Object[] objects= actionsGraphList.get(i);
                Map<String, Object> map = new HashMap<>();
                if (objects[1] == null || objects[2] == null) {
                    objects[1] = "";
                    objects[2] = "";
                }
                map.put("Count", ((BigDecimal) objects[0]).longValue());
                map.put("Source", objects[2].toString());
                map.put("Destination", objects[1].toString());
                actionGraphjsonList.add(map);
            }
        }

        SimpleDateFormat customFormat = new SimpleDateFormat("MM/dd/yyy");
        model.addAttribute("fromDate", customFormat.format(fromDate));
        model.addAttribute("toDate", customFormat.format(toDate));
        model.addAttribute("actionsGraphList", actionsGraphList);

        return actionGraphjsonList;
    }


    @RequestMapping(value = "/actionsCallingServiceGraph", method = RequestMethod.GET)
    public String actionsServiceCallingGraph(Model model) {
        getJsonForActionsServiceCallingGraph(model);
        return "reports/action-calling-service-graph";
    }

    /*Using map to save the key value pair, if either source or destination is null ,
    it keeps the empty string to avoid null pointer exception
    *
    * */


    @RequestMapping(value = "/jsonServiceCallingGraphData", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getJsonForActionsServiceCallingGraph(Model model) {
        Date[] dates = getSessionDates();
        Date fromDate = dates[0];
        Date toDate = dates[1];

        List<Object[]> actionsServiceCallingGraphList = reportRepository.actionsServiceCallingGraph(fromDate, toDate);

        List<Map<String, Object>> actionCallingServiceGraphjsonList = new ArrayList<Map<String, Object>>();
        for (Object[] objects : actionsServiceCallingGraphList) {
            {
                Map<String, Object> map = new HashMap<>();
                if (objects[1]==null) {
                    objects[1] ="";                }
                map.put("Count",((BigDecimal) objects[0]).longValue());
                map.put("CallerName", objects[1].toString());
                actionCallingServiceGraphjsonList.add(map);
            }
        }

        SimpleDateFormat customFormat = new SimpleDateFormat("MM/dd/yyy");
        model.addAttribute("fromDate", customFormat.format(fromDate));
        model.addAttribute("toDate", customFormat.format(toDate));
        model.addAttribute("actionsServiceCallingGraphList", actionsServiceCallingGraphList);

        return actionCallingServiceGraphjsonList;
    }


    private String getDeviceDestinationAndRestoreModel(String model) {

        if (model.contains("4111N")) {
            model = "4111N";

        } else if (model.contains("3347-02")) {
            model = "3347-02";

        } else if (model.contains("7550")) {
            model = "7550";

        } else if (model.contains("NVG510")) {
            model = "NVG510";

        } else if (model.contains("2701HGV-B")) {
            model = "2701HGV-B";

        } else if (model.contains("3600")) {
            model = "3600";

        } else if (model.contains("3800")) {
            model = "3800";

        } else if (model.contains("3801")) {
            model = "3801";

        } else if (model.contains("5031")) {
            model = "5031";

        } else if (model.contains("3812")) {
            model = "3812";

        } else if (model.contains("5168")) {
            model = "5168";

        } else if (model.contains("NVG589")) {
            model = "NVG589";

        } else if (model.contains("5268AC")) {
            model = "5268AC";

        } else if (model.contains("NVG599")) {
            model = "NVG599";

        } else if (model.contains("NVG3150")) {
            model = "NVG3150";

        } else if (model.contains("BGW210-700")) {
            model = "BGW210-700";

        } else if (model.contains("BGW220-100")) {
            model = "BGW220-100";

        } else {
            model = null;

        }
        return model;
    }

    @RequestMapping(value = "/daterange", method = RequestMethod.GET)
    public String dateRange() {
        return "reports/date-range";
    }

    @RequestMapping(value = "/setdaterange", method = RequestMethod.POST)
    public String setDateRange(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, Model model) {
        try {

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(DATE_FORMAT.parse(startDate));
            startCal.set(Calendar.HOUR, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 1);
            Date startDateObj = startCal.getTime();

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(DATE_FORMAT.parse(endDate));
            endCal.set(Calendar.HOUR, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            Date endDateObj = endCal.getTime();

            httpSession.setAttribute("startDate", startDateObj);
            httpSession.setAttribute("endDate", endDateObj);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat customFormat = new SimpleDateFormat("MM/dd/yyy");

        Date fromDate = (Date) httpSession.getAttribute("startDate");
        Date toDate = (Date) httpSession.getAttribute("endDate");

        model.addAttribute("startDate", customFormat.format(fromDate));
        model.addAttribute("endDate", customFormat.format(toDate));
        return "home";
    }

    /*Setting date in session*/
    public Date[] getSessionDates() {
        Date fromDate = (Date) httpSession.getAttribute("startDate");
        Date toDate = (Date) httpSession.getAttribute("endDate");

        //for testing
        /*SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        try {
            fromDate = format.parse("08/23/2016");
            toDate = format.parse("08/24/2016");
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        if (fromDate == null || toDate == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -7);
            fromDate = cal.getTime();
            toDate = new Date();
        }

        return new Date[]{fromDate, toDate};
    }


    @RequestMapping(value = "/download-actionlist", method = RequestMethod.GET)
    public void actionListCsvOutPutWritePoi(HttpServletResponse response) {
        final String fileName = "Actionlist" + ".xlsx";
        Date[] dates = getSessionDates();
        Date fromDate = dates[0];
        Date toDate = dates[1];
        List<ActionList> actionListRepo = reportRepository.actionList(fromDate, toDate);
        List<ActionList> filteredList = new ArrayList<>();
        Iterator<ActionList> iterator = actionListRepo.iterator();
        while (iterator.hasNext()) {
            ActionList actionList = iterator.next();
            if (!((actionList.getBanId() == null) || (actionList.getMemberId() == null) ||
                    (actionList.getResult() == null) || (actionList.getDeviceRestoreSerial() == null) ||
                    (actionList.getDeviceModel() == null) || (actionList.getDeviceRestoreModel() == null) ||
                    (actionList.getCollectionTime() == null) || (actionList.getCallName() == null) || (actionList.getError() == null))) {

                filteredList.add(actionList);
            }
        }
        if (filteredList != null) {
            String deviceDestinationModelLocal = "";
            String deviceRestoreModelLocal = "";
            for (ActionList list : filteredList) {
                deviceDestinationModelLocal = list.getDeviceModel();
                deviceRestoreModelLocal = list.getDeviceRestoreModel();
            }
            String deviceDestinationModel = getDeviceDestinationAndRestoreModel(deviceDestinationModelLocal);
            String deviceRestoreModel = getDeviceDestinationAndRestoreModel(deviceRestoreModelLocal);
           ;
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();

            CellStyle  cellStyle = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setItalic(true);
            cellStyle.setFont(font);

            Row header = sheet.createRow(0);
            header.createCell(0).setCellStyle(cellStyle);
            header.createCell(0).setCellValue("CallerName");
            header.createCell(0).setCellValue("CallerName");
            header.createCell(1).setCellValue("Ban ID");
            header.createCell(2).setCellValue("CallerIP");
            System.out.println("Creating excel");
                int rowCount = 0;
                for (ActionList list: filteredList) {
                    Row row = sheet.createRow(++rowCount);
                    writeActionList(list, row);
                }
            try {
                response.setContentType("application/vnd.openxml");
                response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
               // FileOutputStream outputStream = new FileOutputStream(fileName);
                ServletOutputStream out = response.getOutputStream();

                workbook.write(response.getOutputStream());
                workbook.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
             System.out.println("Done");
        }
    }
    private void writeActionList(ActionList actionList, Row row) {
        Cell cell = row.createCell(0);
        cell.setCellValue(actionList.getCallName());

        cell = row.createCell(1);
        cell.setCellValue(actionList.getBanId());

        cell = row.createCell(2);
        cell.setCellValue(actionList.getCallerIP());
    }

        @RequestMapping(value = "/download-actionlist2", method = RequestMethod.GET)
    public void actionListCsvOutPutPoi(HttpServletResponse response, Pageable pageable) {

        File myFile = new File("C://Office/Employee.xlsx");
        try {
            FileInputStream fis = new FileInputStream(myFile);

            // Finds the workbook instance for XLSX file
            XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

            // Return first sheet from the XLSX workbook
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            // Get iterator to all the rows in current sheet
            Iterator<Row> rowIterator = mySheet.iterator();

            // Traversing over each row of XLSX file
            while (rowIterator.hasNext())
            { Row row = rowIterator.next();

                // For each row, iterate through each columns
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext())
                { Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            System.out.print(cell.getStringCellValue() + "\t");
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            System.out.print(cell.getNumericCellValue() + "\t");
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            System.out.print(cell.getBooleanCellValue() + "\t");
                            break;
                        default : }
                }
                System.out.println(""); }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException ie)
        {
          ie.printStackTrace();
        }
    }

    /*Action List CSV*/

    @RequestMapping(value = "/download-actionlist1", method = RequestMethod.GET)
    public void actionListCsvOutPut(HttpServletResponse response, Pageable pageable) {
        String fileName = "Actionlist" + ".csv";
        try {
            String newline = "\n";
            StringBuilder csvOutput = new StringBuilder("Id,Type,Result,Created,Ban Id,Member Id,Device Dest Model,Device Dest Serial,Device Source Model,Device Source Serial,Called From,Collection Time,Error").append(newline);

            Date[] dates = getSessionDates();
            Date fromDate = dates[0];
            Date toDate = dates[1];

            List<ActionList> actionListRepo = reportRepository.actionList(fromDate,toDate);
            List<ActionList> filteredList = new ArrayList<>();

            Iterator<ActionList> iterator = actionListRepo.iterator();
            while (iterator.hasNext())
            {
                ActionList actionList = iterator.next();

                if ( !((actionList.getBanId() == null) || (actionList.getMemberId() == null) ||
                        (actionList.getResult() == null) || (actionList.getDeviceRestoreSerial() == null) ||
                        (actionList.getDeviceModel() == null) || (actionList.getDeviceRestoreModel() == null) ||
                        (actionList.getCollectionTime() == null) || (actionList.getCallName() == null) || (actionList.getError() == null)) ){

                    filteredList.add(actionList);
                }
            }

            if (filteredList != null) {

                String deviceDestinationModelLocal = "";
                String deviceRestoreModelLocal = "";

                for (ActionList list : filteredList) {
                    deviceDestinationModelLocal = list.getDeviceModel();
                    deviceRestoreModelLocal = list.getDeviceRestoreModel();
                }

                String deviceDestinationModel = getDeviceDestinationAndRestoreModel(deviceDestinationModelLocal);
                String deviceRestoreModel = getDeviceDestinationAndRestoreModel(deviceRestoreModelLocal);

                for (ActionList list : filteredList) {
                    Date date = list.getCreated();
                    csvOutput.append(list.getId()).append(",");
                    csvOutput.append(list.getType()).append(",");
                    csvOutput.append(list.getResult()).append(",");
                    csvOutput.append(date).append(",");
                    csvOutput.append(list.getBanId()).append(",");
                    csvOutput.append(list.getMemberId()).append(",");
                    csvOutput.append(deviceDestinationModel).append(",");
                    csvOutput.append(list.getDeviceSerial()).append(",");
                    csvOutput.append(deviceRestoreModel).append(",");
                    csvOutput.append(list.getDeviceRestoreSerial()).append(",");
                    csvOutput.append(list.getCallName()).append(",");
                    csvOutput.append(list.getCollectionTime()).append(",");
                    csvOutput.append(list.getError()).append(",");
                    csvOutput.append("").append(",");
                    csvOutput.append(newline);

                }
            }
            //to send file content to browser
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            ServletOutputStream out = response.getOutputStream();

            byte[] outputByte = csvOutput.toString().getBytes();
            out.write(outputByte, 0, outputByte.length);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*Action summary CSV*/
    @RequestMapping(value = "/download-actionsummary", method = RequestMethod.GET)
    public void actionSummaryCsvOutPut(HttpServletResponse response) {
        String fileName = "Action-summary" + ".csv";
        try {
            String newline = "\n";
            StringBuilder csvOutput = new StringBuilder("Type,Total,Success,Failure,Error,Timeout,Non-Timeout").append(newline);
            Date[] dates = getSessionDates();
            Date fromDate = dates[0];
            Date toDate = dates[1];
            List<Object[]> actionSummaryList = reportRepository.actionSummary(fromDate, toDate);
            if (actionSummaryList != null) {
                long totalCount = 0;
                long successCount = 0;
                long failureCount = 0;
                long errorCount = 0;
                long timeoutCount = 0;
                long nonTimeOutCount = 0;
                for (Object[] list : actionSummaryList) {

                    totalCount = totalCount + ((BigDecimal) list[1]).longValue();
                    successCount = successCount + ((BigDecimal) list[2]).longValue();
                    failureCount = failureCount + ((BigDecimal) list[3]).longValue();
                    errorCount = errorCount + ((BigDecimal) list[4]).longValue();
                    timeoutCount = timeoutCount + ((BigDecimal) list[5]).longValue();
                    nonTimeOutCount = nonTimeOutCount + ((BigDecimal) list[6]).longValue();
                }



                for (Object[] list : actionSummaryList) {
                    String result = (String) list[0];

                    csvOutput.append(list[0]).append(",");
                    csvOutput.append(list[1]).append(",");
                    csvOutput.append(list[2]).append(",");
                    csvOutput.append(list[3]).append(",");
                    csvOutput.append(list[4]).append(",");
                    csvOutput.append(list[5]).append(",");
                    csvOutput.append(list[6]).append(",");
                    csvOutput.append("").append(",");
                    csvOutput.append(newline);

                }
                csvOutput.append("Totals").append(",");
                csvOutput.append(totalCount).append(",");
                csvOutput.append(successCount).append(",");
                csvOutput.append(failureCount).append(",");
                csvOutput.append(errorCount).append(",");
                csvOutput.append(timeoutCount).append(",");
                csvOutput.append(nonTimeOutCount);

            }

            //to send file content to browser
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            ServletOutputStream out = response.getOutputStream();

            byte[] outputByte = csvOutput.toString().getBytes();
            out.write(outputByte, 0, outputByte.length);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*Action Restoral calls CSV*/
    @RequestMapping(value = "/download-actionrestorecalls", method = RequestMethod.GET)
    public void actionRestoreCallsCsvOutPut(HttpServletResponse response) {
        String fileName = "Action-Restorecalls" + ".csv";
        try {
            String newline = "\n";
            StringBuilder csvOutput = new StringBuilder("Step Name,Total Number of Transactions,Total % of Transactions").append(newline);
            Object[] returnData = restoreCallDtoList();
            Collection<RestoreCallDto> returnRestoreList = ((Map<String, RestoreCallDto>)returnData[0]).values();
            List<RestoreCallDto> toCsvList = new ArrayList<>();
            for (RestoreCallDto r: returnRestoreList) {
                String prepend = "";
                if (r.getStepName().startsWith("E-")) {
                    prepend = "Total Errors -";
                } else if (r.getStepName().startsWith("T-")) {
                    prepend = "Restores called -";
                } else if (r.getStepName().startsWith("F-")) {
                    prepend = "Failed - ";
                } else if (r.getStepName().startsWith("S-")) {
                    prepend = "Success - ";
                }
                r.setDisplayName(prepend + r.getDisplayName());
                toCsvList.add(r);
            }

            Collections.sort(toCsvList, new Comparator<RestoreCallDto>() {
                @Override
                public int compare(RestoreCallDto o1, RestoreCallDto o2) {
                    String prepend1 = null;
                    if (o1.getDisplayName().indexOf("-") > 0) {
                        prepend1 = o1.getDisplayName().substring(0, o1.getDisplayName().indexOf('-'));
                    }
                    else prepend1 = o1.getDisplayName();

                    String prepend2 = null;
                    if (o2.getDisplayName().indexOf("-") > 0) {
                        prepend2 = o2.getDisplayName().substring(0, o2.getDisplayName().indexOf('-'));
                    }
                    else prepend2 = o2.getDisplayName();

                    int compareToResult = sortOrder(prepend1).compareTo(sortOrder(prepend2));
                    if (compareToResult == 0){
                        return o1.getDisplayName().compareTo(o2.getDisplayName());
                    }
                    else return compareToResult;
                }

                private Integer sortOrder(String prepend){
                    int sortOrder = -1;
                    if (prepend.startsWith("Restores called")){
                        sortOrder = 1;
                    }
                    else if (prepend.startsWith("Success")){
                        sortOrder = 2;
                    }
                    else if (prepend.startsWith("Failed")){
                        sortOrder = 3;
                    }
                    else if (prepend.startsWith("Total Errors")){
                        sortOrder = 4;
                    }
                    return sortOrder;
                }
            });

            if (toCsvList != null) {
                for (RestoreCallDto list : toCsvList) {
                    csvOutput.append(list.getDisplayName()).append(",");
                    csvOutput.append(list.getTransCount()).append(",");
                    csvOutput.append(list.getTransPerc()).append(",");
                    csvOutput.append("").append(",");
                    csvOutput.append(newline);
                }
            }

            List<RestoreCallDto> timeoutCallDtos = (List<RestoreCallDto>)returnData[1];
            if(timeoutCallDtos!=null){

                for(RestoreCallDto restoreCallDto:timeoutCallDtos){

                    csvOutput.append(restoreCallDto.getDisplayName()).append(",");
                    csvOutput.append(restoreCallDto.getTransCount()).append(",");
                    csvOutput.append(restoreCallDto.getTransPerc()).append(",");
                    csvOutput.append("").append(",");
                    csvOutput.append(newline);
                }
            }

            //to send file content to browser
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            ServletOutputStream out = response.getOutputStream();
            byte[] outputByte = csvOutput.toString().getBytes();
            out.write(outputByte, 0, outputByte.length);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*Actions graph CSV*/
    @RequestMapping(value = "/download-actionsGraph", method = RequestMethod.GET)
    public void actionsGraphCsvOutPut(HttpServletResponse response) {
        String fileName = "Actions Graph" + ".csv";
        try {
            String newline = "\n";
            StringBuilder csvOutput = new StringBuilder("Source Model,Destination Model, Count").append(newline);

            Date[] dates = getSessionDates();
            Date fromDate = dates[0];
            Date toDate = dates[1];


            List<Object[]> actionsGraphList = reportRepository.actionsGraph(fromDate, toDate);
            if (actionsGraphList != null) {

                for (Object[] objects : actionsGraphList) {
                    if (objects[1] == null || objects[2] == null) {
                        objects[1] = "";
                        objects[2] = "";
                    }
                    csvOutput.append(objects[2].toString()).append(",");
                    csvOutput.append(objects[1].toString()).append(",");
                    csvOutput.append(objects[0].toString()).append(",");
                    csvOutput.append("").append(",");
                    csvOutput.append(newline);

                }
            }
            //to send file content to browser
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            ServletOutputStream out = response.getOutputStream();

            byte[] outputByte = csvOutput.toString().getBytes();
            out.write(outputByte, 0, outputByte.length);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*Actions graph CSV*/
    @RequestMapping(value = "/download-actionsServiceCallingGraph", method = RequestMethod.GET)
    public void actionsServiceCallingGraphCsvOutPut(HttpServletResponse response) {
        String fileName = "Service Calling Graph" + ".csv";
        try {
            String newline = "\n";
            StringBuilder csvOutput = new StringBuilder("Caller Name,Count").append(newline);

            Date[] dates = getSessionDates();
            Date fromDate = dates[0];
            Date toDate = dates[1];


            List<Object[]> actionsServiceCallingGraphList = reportRepository.actionsServiceCallingGraph(fromDate, toDate);
            if (actionsServiceCallingGraphList != null) {

                for (Object[] objects : actionsServiceCallingGraphList) {
                    if (objects[1] == null) {
                        objects[1] = "";

                    }
                    csvOutput.append(objects[1].toString()).append(",");
                    csvOutput.append(objects[0].toString()).append(",");
                    csvOutput.append("").append(",");
                    csvOutput.append(newline);

                }
            }
            //to send file content to browser
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            ServletOutputStream out = response.getOutputStream();

            byte[] outputByte = csvOutput.toString().getBytes();
            out.write(outputByte, 0, outputByte.length);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] d) {
        System.out.println((long) 271730 * 10000);
        double percX100 = ((((long) 271730) * ((long) 10000))) / 482480;
        System.out.println(percX100);
    }
}

