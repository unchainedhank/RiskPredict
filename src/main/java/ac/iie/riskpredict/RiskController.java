package ac.iie.riskpredict;

import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ac.iie.riskpredict.RiskServiceImpl.output;


/**
 * @author a3
 */
@RestController
public class RiskController {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @javax.annotation.Resource

    private RiskServiceImpl service;
    @javax.annotation.Resource
    private SupplyRiskDao dao;
    @javax.annotation.Resource

    private LogCleaner logCleaner;

    private long count = 1;

    //更新锁，如果请求的时候正在更新=1
    volatile AtomicBoolean isUpdating = new AtomicBoolean(false); // 定义一个原子整型变量
    volatile AtomicInteger updateCurrentCount = new AtomicInteger(0); // 定义一个原子整型变量

    volatile static public AtomicBoolean isTraining = new AtomicBoolean(false); // 定义一个原子整型变量
    volatile static public AtomicInteger trainCurrentCount = new AtomicInteger(0); // 定义一个原子整型变量
    @Value("${my.log-path}")
    private String configuredLogPath;


    @RequestMapping(method = RequestMethod.GET, value = "/risk/update")
    public String update() {
        logger.info("系统准备更新");
        if (isUpdating.get()) return "正在更新中，待更新结束后再申请更新";
        try {
            isUpdating.set(true);
            count = Math.max(1, dao.count());
            //更新数据-遍历所有dao的函数


            int pageSize = 500; // 每页查询的记录数
            long totalPage = (int) Math.ceil(count / (double) pageSize); // 总页数

            ExecutorService executorService = Executors.newFixedThreadPool(10); // 创建一个可缓存的线程池


            logger.info("成功创建线程池：" + executorService);
            for (int currentPage = 0; currentPage < totalPage; currentPage++) {
                final int page = currentPage;
                executorService.submit(() -> {
                    PageRequest pageRequest = PageRequest.of(page, pageSize);
                    logger.info("创建查询请求" + pageRequest.getPageNumber() + " " + pageRequest.getPageSize());
                    Page<SupplierRisk> riskPage = dao.findAll(pageRequest);

                    logger.info("查询返回结果：" + riskPage.getContent().toArray().length + "条");
                    for (SupplierRisk r : riskPage.getContent()) {
                        computeFeatures(r);
                        logger.info("已更新" + r.getSupply_id());
                        updateCurrentCount.getAndIncrement();
                    }
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            int s = updateCurrentCount.get();
            isUpdating.set(false);
            updateCurrentCount.set(0);
            return "更新完成" + "，共更新记录" + s + "条";
        } catch (Exception e) {
            isUpdating.set(false);
            updateCurrentCount.set(0);
            return "更新失败" + " 失败原因：" + e.getMessage();
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/risk/updateProgress")
    public String getUpdateProgress() {
        return isUpdating.get() ? "更新中，当前进度：" + updateCurrentCount.get() + "/" + count : "当前系统不在更新中";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/risk/getLog")
    public String getLog() {
        File logFile = new File(configuredLogPath);
        try {
            if (logFile.exists()) {
                logger.info("日志文件存在：" + configuredLogPath);
                return FileUtils.readFileToString(logFile, "UTF-8");
            } else {
                // 从根路径开始查找日志文件
                logger.warn("日志文件不存在：" + configuredLogPath);

                File rootLogFile = findLogFile("spring.log", new File("/app/"));
                if (rootLogFile != null) {
                    logger.info("成功找到日志文件：" + rootLogFile.getAbsolutePath());
                    configuredLogPath = rootLogFile.getPath();
                    return FileUtils.readFileToString(rootLogFile, "UTF-8");
                } else logger.warn("找不到日志文件");
            }
        } catch (IOException e) {
            logger.warn("日志位置：" + configuredLogPath);
            return "读取日志失败" + " 失败原因：" + e.getMessage();
        }
        return "未找到日志文件";
    }

    private File findLogFile(String fileName, File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals(fileName)) {
                    return file;
                } else if (file.isDirectory()) {
                    File result = findLogFile(fileName, file);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/risk/predict")
    public String predict(@RequestBody SupplierRisk risk) {
        try {
            PredictBean predictBean = new PredictBean();
            BeanUtils.copyProperties(predictBean, risk);
            //执行特征工程
            featureEngineering(risk);
            BeanUtils.copyProperties(predictBean, dao.findById(risk.getSupply_id()));
            logger.warn("特征数据：" + predictBean);
            Double[] featureArray = convert2Array(predictBean);
            String risk_feature = JSON.toJSONString(featureArray);

            String pythonPath = "/app/classes/static/predict.py";
//            String pythonPath = "/Users/a3/IdeaProjects/RiskPredict/src/main/resources/static/predict.py";

            String[] arguments = new String[]{"python3", pythonPath, risk_feature};//指定命令、路径、传递的参数
            logger.warn("system cmd: " + Arrays.toString(arguments));
            return service.invokePython(arguments);
        } catch (Exception e) {
            return "预测失败" + " 失败原因：" + e.getMessage();
        }

    }

    private Double[] convert2Array(PredictBean bean) {
        // 获取类的所有属性
        Field[] fields = bean.getClass().getDeclaredFields();
        Double[] values = new Double[fields.length];
        try {
            for (int i = 0; i < fields.length; i++) {
                String name = fields[i].getName();
                if (name.equals("name") || name.contains("date") || name.equals("city")) {
                    continue;
                }
                // 设置属性可访问
                fields[i].setAccessible(true);
                Object fieldValue = fields[i].get(bean);
                if (fieldValue != null) {
                    if (fieldValue instanceof Integer) {
                        // 将整数类型的属性转换为字符串类型
                        values[i] = Double.valueOf(Integer.toString((Integer) fieldValue));
                    } else if (("").equals(fieldValue)) {
                        values[i] = 0.0;
                    } else {
                        // 直接将属性的值作为字符串来处理
                        values[i] = Double.valueOf(fieldValue.toString());
                    }
                } else {
                    // 如果属性为null，则填0
                    values[i] = 0.0;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return values;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/risk/train")
    public String train() {
        if (isTraining.get()) {
            return "系统正在训练中";
        }
        isTraining.set(true);
        service.trainModel();
        return "系统开始训练";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/risk/trainProgress")
    public String getTrainProgress() {
        if (isTraining.get()) {
            ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("supply_id"); // 忽略 supply_id 属性
            Example<SupplierRisk> example1 = Example.of(SupplierRisk.builder().sample_class(1).build(), matcher);
            long count1 = dao.count(example1);
            Example<SupplierRisk> example0 = Example.of(SupplierRisk.builder().sample_class(0).build(), matcher);
            long count0 = dao.count(example0);
            long i = trainCurrentCount.get();
            long j = count0 + count1;
            if (i < j*0.955) {
                return "正在获取训练数据，进度：" + i + "/" + j;
            } else {
                return "正在训练，进度：" + output.toString();
            }
        } else return "当前系统不在训练中";
    }

    //    public String train() {
//        String[] TRAIN_HEADER = {"supply_id", "sample_class", "supplier_name", "supply_code", "supplier_id",
//                "tender_id", "contract_cycle", "avg_history_contract_cycle", "plan_cycle", "avg_yearly_plan_cycle",
//                "history_plan_cycle", "rank_pack_unit_price", "vari_pack_unit_price", "avg_pack_unit_price",
//                "max_pack_unit_price", "unit_price", "vari_yearly_unit_price", "total_price",
//                "avg_history_total_price", "vari_history_total_price", "rank_history_total_price",
//                "rank_yearly_total_price", "supply_num", "vari_history_num", "max_history_num", "rank_yearly_num",
//                "history_fulfill_rate", "fulfill_times", "history_breach_rate", "history_breach_times",
//                "max_history_breach_amount", "sum_history_breach_amount", "avg_history_breach_amount", "row_material"
//                , "punish_times", "sum_punish", "register_capital", "city", "supply_history_length",
//                "legal_assist_times", "land_mortgage_area", "overdue_tax", "lawsuit_times", "sum_lawsuit",
//                "abnormal_operation_times", "mission_accept_date", "contract_date", "plan_date", "delivery_date",
//                "real_delivery_date", "supply_name"};
//        List<SupplierRisk> allTrainData = dao.findAll();
//        File trainCsv = new File("/app/classes/static/t_supply_risk.csv");
//        trainCsv.delete();
//        List<String> csvHeader = Arrays.asList(TRAIN_HEADER);
//        CSVFormat format = CSVFormat.DEFAULT.withHeader(TRAIN_HEADER).withSkipHeaderRecord();
//        //创建数据集
//        try (Writer out = new FileWriter("/app/classes/static/t_supply_risk.csv");
//             CSVPrinter printer = new CSVPrinter(out, format)) {
//            printer.printRecord(csvHeader);
//            for (SupplierRisk risk : allTrainData) {
//                List<String> records = new ArrayList<>();
//                Field[] fields = risk.getClass().getDeclaredFields();
//                for (Field f :
//                        fields) {
//                    f.setAccessible(true);
//                    Object value = f.get(risk);
//                    if (value == null) {
//                        records.add("");
//                    } else if (value instanceof Integer || value instanceof Double) {
//                        records.add(String.valueOf(value));
//                    } else if (value instanceof Timestamp) {
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                        records.add(dateFormat.format(value));
//                    } else {
//                        records.add((String) value);
//                    }
//                }
//                printer.printRecord(records);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "训练失败" + " 失败原因：" + e.getMessage();
//        }
//        String pythonPath = "/app/classes/static/train.py";
//        //指定命令、路径、传递的参数
//        String[] arguments = new String[]{"python3", pythonPath};
//        return invokePython(arguments);
//    }

    private void featureEngineering(@RequestBody SupplierRisk risk) {
//        Class<?> engineering = service.getClass();
//        Method[] methods = engineering.getDeclaredMethods();
//        for (Method method : methods) {
//            try {
//                method.invoke(engineering.newInstance());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        computeFeatures(risk);

    }

    private void computeFeatures(SupplierRisk risk) {
        String supply_id = risk.getSupply_id();
        String supply_code = risk.getSupply_code();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(risk.getContract_date());
        int year1 = calendar.get(Calendar.YEAR);
        String year = Integer.toString(year1);
        service.contract_cycle(supply_id);
        service.avg_history_contract_cycle(supply_id);
        service.plan_cycle(supply_id);
        service.avg_yearly_plan_cycle(supply_id, year, supply_code);
        service.history_plan_cycle(supply_id);
        service.avg_pack_unit_price(supply_id);
        service.vari_pack_unit_price(supply_id);
        service.rank_pack_unit_price(supply_id);
        service.max_pack_unit_price(supply_id);
        service.vari_yearly_unit_price(supply_id, year);
        service.avg_history_total_price(supply_id);
        service.vari_history_total_price(supply_id);
        service.rank_history_total_price(supply_id);
        service.rank_yearly_total_price(supply_id, year);
        service.supply_num(supply_id);
        service.vari_history_num(supply_id);
        service.max_history_num(supply_id);
        service.rank_yearly_num(supply_id, year);
        service.history_fulfill_rate(supply_id);
        service.fulfill_times(supply_id);
        service.history_breach_rate(supply_id);
        service.history_breach_times(supply_id);
        service.max_history_breach_amount(supply_id);
        service.sum_history_breach_amount(supply_id);
        service.avg_history_breach_amount(supply_id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/risk/cleanLogs")
    public String cleanLogs() throws IOException {
        logCleaner.cleanLogs();
        return "log cleaned";
    }

}

