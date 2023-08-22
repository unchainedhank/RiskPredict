package ac.iie.riskpredict;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ac.iie.riskpredict.RiskServiceImpl.*;


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

    public static long count = 1;

    //更新锁，如果请求的时候正在更新=1
    public static volatile AtomicBoolean isUpdating = new AtomicBoolean(false); // 定义一个原子整型变量
    public static volatile AtomicInteger updateCurrentCount = new AtomicInteger(0); // 定义一个原子整型变量

    volatile static public AtomicBoolean isTraining = new AtomicBoolean(false); // 定义一个原子整型变量
    volatile static public AtomicInteger trainCurrentCount = new AtomicInteger(0); // 定义一个原子整型变量
    @Value("${my.log-path}")
    private String configuredLogPath;


    @RequestMapping(method = RequestMethod.GET, value = "/risk/update")
    public String update() {
        if (isUpdating.get()) {
            return "正在更新中，待更新结束后再申请更新";
        }
        service.update();
        return "系统开始更新于：" + DateTime.now();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/risk/updateProgress")
    public String getUpdateProgress() {
        if (isUpdating.get()) {
            return "更新中，当前进度：" + updateCurrentCount.get() + "/" + count;
        } else if (updateOutput.toString().contains("更新完成")) {
            String s = updateOutput.toString();
            updateOutput.setLength(0);
            updateError.setLength(0);
            return s;
        } else if (updateError.toString().contains("更新失败")) {
            String s = updateError.toString();
            updateOutput.setLength(0);
            updateError.setLength(0);
            return s;
        } else {
            return "当前系统不在更新中";
        }

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
            service.computeFeatures(risk);
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
        return "系统开始训练于" + DateTime.now();
    }


    @RequestMapping(method = RequestMethod.GET, value = "/risk/trainProgress")
    public String getTrainProgress() {
        // 正在训练
        if (isTraining.get()) {
            ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("supply_id"); // 忽略 supply_id 属性
            Example<SupplierRisk> example1 = Example.of(SupplierRisk.builder().sample_class(1).build(), matcher);
            long count1 = dao.count(example1);
            Example<SupplierRisk> example0 = Example.of(SupplierRisk.builder().sample_class(0).build(), matcher);
            long count0 = dao.count(example0);
            long i = trainCurrentCount.get();
            long j = count0 + count1;
            // 获取数据
            if (i < j * 0.955) {
                return "正在获取训练数据，进度：" + i + "/" + j;
            }
            // 调用python训练
            else {
                if (invokePythonOutput.toString().contains("失败")) {
                    return "训练失败于" + DateTime.now() + "训练信息:\n" + invokePythonError;
                } else {
                    if (invokePythonOutput.length() < 1) {
                        return "正在训练，等待超参数训练器启动";
                    } else return "正在训练，进度：" + invokePythonOutput;
                }
            }
        }
        // 训练完成
        else if (trainTimer.toString().contains("训练完成")) {

            String s = trainTimer.toString();
            return s + "训练信息:\n" + invokePythonOutput.toString();

        }
        // 不在训练
        else return "当前系统不在训练中";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/risk/cleanLogs")
    public String cleanLogs() throws IOException {
        logCleaner.cleanLogs();
        return "log cleaned";
    }

}

