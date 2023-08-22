package ac.iie.riskpredict;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ac.iie.riskpredict.RiskController.*;

/**
 * @author a3
 */
@Service
public class RiskServiceImpl {
    @Resource
    private SupplyRiskDao dao;

    Logger logger = LoggerFactory.getLogger(this.getClass());


    public static StringBuilder invokePythonOutput = new StringBuilder();
    public static StringBuilder invokePythonError = new StringBuilder();

    public static StringBuilder updateOutput = new StringBuilder();
    public static StringBuilder updateError = new StringBuilder();
    public static StringBuilder trainTimer = new StringBuilder();


    public void contract_cycle(String supplyId) {
        dao.contract_cycle(supplyId);
    }


    public void avg_history_contract_cycle(String supplyId) {
        dao.avg_history_contract_cycle(supplyId);
    }


    public void plan_cycle(String supplyId) {
        dao.planCycle(supplyId);
    }


    public void avg_yearly_plan_cycle(String supplyId, String year, String supplyCode) {
        dao.avg_yearly_plan_cycle(supplyId, year, supplyCode);
    }


    public void history_plan_cycle(String supplyId) {
        dao.history_plan_cycle(supplyId);

    }


    public void avg_pack_unit_price(String supplyId) {
        dao.avg_pack_unit_price(supplyId);

    }


    public void vari_pack_unit_price(String supplyId) {
        dao.vari_pack_unit_price(supplyId);
    }


    public void rank_pack_unit_price(String supplyId) {
        dao.rank_pack_unit_price(supplyId);
    }


    public void max_pack_unit_price(String supplyId) {
        dao.max_pack_unit_price(supplyId);

    }


    public void vari_yearly_unit_price(String supplyId, String year) {
        dao.vari_yearly_unit_price(supplyId, year);
    }

    public void avg_history_total_price(String supplyId) {
        dao.avg_history_total_price(supplyId);
    }

    public void vari_history_total_price(String supplyId) {
        dao.vari_history_total_price(supplyId);
    }


    public void rank_history_total_price(String supplyId) {
        dao.rank_history_total_price(supplyId);
    }


    public void rank_yearly_total_price(String supplyId, String year) {
        dao.rank_yearly_total_price(supplyId, year);
    }


    public void supply_num(String supplyId) {
        dao.supply_num(supplyId);
    }

    public void vari_history_num(String supplyId) {
        dao.vari_history_num(supplyId);
    }

    public void max_history_num(String supplyId) {
        dao.max_history_num(supplyId);
    }

    public void rank_yearly_num(String supplyId, String year) {
        dao.rank_yearly_num(supplyId, year);
    }

    public void history_fulfill_rate(String supplyId) {
        dao.history_fulfill_rate(supplyId);
    }

    public void fulfill_times(String supplyId) {
        dao.fulfill_times(supplyId);
    }

    public void history_breach_rate(String supplyId) {
        dao.history_breach_rate(supplyId);
    }

    public void history_breach_times(String supplyId) {
        dao.history_breach_times(supplyId);
    }

    public void max_history_breach_amount(String supplyId) {
        dao.max_history_breach_amount(supplyId);
    }

    public void sum_history_breach_amount(String supplyId) {
        dao.sum_history_breach_amount(supplyId);
    }

    public void avg_history_breach_amount(String supplyId) {
        dao.avg_history_breach_amount(supplyId);
    }

    @Async
    public void trainModel() {
        try {
            TimeInterval timer = DateUtil.timer();
//            File trainCsv = new File("/Users/a3/IdeaProjects/RiskPredict/src/main/resources/static/t_supply_risk.csv");
            File trainCsv = new File("/app/classes/static/t_supply_risk.csv");
            if (trainCsv.delete()) logger.info("成功删除旧文件");
            else logger.info("删除旧文件失败");


//            CSVWriter writer = new CSVWriter(new FileWriter("/Users/a3/IdeaProjects/RiskPredict/src/main/resources/static/t_supply_risk.csv", true));
            CSVWriter writer = new CSVWriter(new FileWriter("/app/classes/static/t_supply_risk.csv", true));
            String header = "supply_id,sample_class,supplier_name,supply_code,supplier_id," +
                    "tender_id,contract_cycle,avg_history_contract_cycle,plan_cycle,avg_yearly_plan_cycle," +
                    "history_plan_cycle,rank_pack_unit_price,vari_pack_unit_price,avg_pack_unit_price," +
                    "max_pack_unit_price,unit_price,vari_yearly_unit_price,total_price," +
                    "avg_history_total_price,vari_history_total_price,rank_history_total_price," +
                    "rank_yearly_total_price,supply_num,vari_history_num,max_history_num,rank_yearly_num," +
                    "history_fulfill_rate,fulfill_times,history_breach_rate,history_breach_times," +
                    "max_history_breach_amount,sum_history_breach_amount,avg_history_breach_amount,row_material," +
                    "punish_times,sum_punish,register_capital,city,supply_history_length," +
                    "legal_assist_times,land_mortgage_area,overdue_tax,lawsuit_times,sum_lawsuit," +
                    "abnormal_operation_times,mission_accept_date,contract_date,plan_date,delivery_date," +
                    "real_delivery_date,supply_name";
            String[] headerArray = header.split(",");
            writer.writeNext(headerArray);


            //构建查询条件
            ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("supply_id"); // 忽略 supply_id 属性
            Example<SupplierRisk> example1 = Example.of(SupplierRisk.builder().sample_class(1).build(), matcher);
            long count1 = dao.count(example1);
            logger.info("履约数" + count1);
            Example<SupplierRisk> example0 = Example.of(SupplierRisk.builder().sample_class(0).build(), matcher);
            long count0 = dao.count(example0);
            logger.info("违约数" + count0);

            ExecutorService executorService = Executors.newFixedThreadPool(10); // 创建一个可缓存的线程池
            logger.info("成功创建线程池：" + executorService);

            int pageSize = 500; // 每页查询的记录数

            long totalPage = (int) Math.ceil(count1 / (double) pageSize); // 总页数
            // 获取履约履约数据
            getTrainData(writer, example1, executorService, pageSize, totalPage);

            // 获取违约履约数据
            totalPage = (int) Math.ceil(count0 / (double) pageSize); // 总页数
            getTrainData(writer, example0, executorService, pageSize, totalPage);

            executorService.shutdown();
            boolean ifAllComplete = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            if (ifAllComplete) {
                writer.close();
                 String pythonPath = "/app/classes/static/train.py";
//                String pythonPath = "/Users/a3/IdeaProjects/RiskPredict/src/main/resources/static/train.py";
                //指定命令、路径、传递的参数
                String[] arguments = new String[]{"python3", pythonPath};
                invokePython(arguments);
                trainTimer.append("训练完成于").append(DateTime.now()).append("共耗时").append(DateUtil.formatBetween(timer.interval(),
                        BetweenFormatter.Level.MINUTE));
                isTraining.set(false);

            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void getTrainData(CSVWriter writer, Example<SupplierRisk> example1, ExecutorService executorService,
                              int pageSize, long totalPage) {
        try {
            for (int currentPage = 0; currentPage < totalPage; currentPage++) {
                final int page = currentPage;
                executorService.submit(() -> {
                    PageRequest pageRequest = PageRequest.of(page, pageSize);
                    logger.info("创建查询请求" + pageRequest.getPageNumber() + " " + pageRequest.getPageSize());
                    Page<SupplierRisk> riskPage = dao.findAll(example1, pageRequest);

                    logger.info("查询返回结果：" + riskPage.getContent().toArray().length + "条");
                    for (SupplierRisk r : riskPage.getContent()) {
                        List<String> builder = new ArrayList<>();
                        Field[] fields = r.getClass().getDeclaredFields();
                        for (Field f : fields) {
                            f.setAccessible(true);
                            Object value;
                            try {
                                value = f.get(r);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                            if (value instanceof Integer || value instanceof Double) {
                                builder.add(value.toString());
                            } else if (value instanceof Timestamp) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                builder.add(dateFormat.format(value));
                            } else {
                                builder.add((String) value);
                            }
                        }
                        writer.writeNext(builder.toArray(new String[0]));
                        trainCurrentCount.getAndIncrement();
                    }
                });
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public String invokePython(String[] arguments) {
        try {
            ProcessBuilder builder = new ProcessBuilder(arguments);
            Process process = builder.start();

            // 获取字符输入流对象
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));
            // 获取错误信息的字符输入流对象
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream(),
                    StandardCharsets.UTF_8));

            String line;
            // 记录输出结果
            while ((line = in.readLine()) != null) {
                invokePythonOutput.append(line).append("\n");
            }

            // 记录错误信息
            while ((line = err.readLine()) != null) {
                invokePythonError.append(line).append("\n");
            }

            in.close();
            err.close();
            process.waitFor();
        } catch (Exception e) {
            String message = invokePythonOutput.toString() + invokePythonError.toString();
            invokePythonOutput.setLength(0);
            invokePythonError.setLength(0);
            return message + e.getMessage();
        }

        String errorMsg = invokePythonError.toString();
        if (!errorMsg.isEmpty()) {
            logger.warn(errorMsg);
            invokePythonError.setLength(0);
            return " 失败原因：" + errorMsg;
        }
        String s = invokePythonOutput.toString();
        invokePythonOutput.setLength(0);
        return s;
    }

    @Async
    public void update() {
        try {
            isUpdating.set(true);
            // 开始计时
            TimeInterval timer = DateUtil.timer();
            count = Math.max(1, dao.count());

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
                        updateCurrentCount.getAndIncrement();
                    }
                });
            }
            executorService.shutdown();
            boolean ifAllComplete = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            if (ifAllComplete) {
                int s = updateCurrentCount.get();
                isUpdating.set(false);
                updateCurrentCount.set(0);
                String a = "更新完成于" + DateUtil.now() + "，共更新记录" + s + "条";
                updateOutput.append(a).append("，共耗时：").append(DateUtil.formatBetween(timer.intervalMs(),
                        BetweenFormatter.Level.MINUTE));
            }
        } catch (Exception e) {
            isUpdating.set(false);
            updateCurrentCount.set(0);
            updateError.append("更新失败于").append(DateUtil.now()).append(" 失败原因：").append(e.getMessage());
        }
    }


    public void computeFeatures(SupplierRisk risk) {
        String supply_id = risk.getSupply_id();
        String supply_code = risk.getSupply_code();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(risk.getContract_date());
        int year1 = calendar.get(Calendar.YEAR);
        String year = Integer.toString(year1);
        contract_cycle(supply_id);
        avg_history_contract_cycle(supply_id);
        plan_cycle(supply_id);
        avg_yearly_plan_cycle(supply_id, year, supply_code);
        history_plan_cycle(supply_id);
        avg_pack_unit_price(supply_id);
        vari_pack_unit_price(supply_id);
        rank_pack_unit_price(supply_id);
        max_pack_unit_price(supply_id);
        vari_yearly_unit_price(supply_id, year);
        avg_history_total_price(supply_id);
        vari_history_total_price(supply_id);
        rank_history_total_price(supply_id);
        rank_yearly_total_price(supply_id, year);
        supply_num(supply_id);
        vari_history_num(supply_id);
        max_history_num(supply_id);
        rank_yearly_num(supply_id, year);
        history_fulfill_rate(supply_id);
        fulfill_times(supply_id);
        history_breach_rate(supply_id);
        history_breach_times(supply_id);
        max_history_breach_amount(supply_id);
        sum_history_breach_amount(supply_id);
        avg_history_breach_amount(supply_id);
    }


}
