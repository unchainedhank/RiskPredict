package ac.iie.riskpredict;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@SuppressWarnings("ALL")
@Slf4j
public class Test {
    public static void predictRiskAndContributions(String str) {
        if (str.contains("Error") || str.contains("Warning")) {
            log.error("invoke python error"+str);
            throw new RuntimeException("调用python预测子程序错误"+str);
        } else {
            str = str.replace("[", "");
            str = str.replace("]", " ");
            String[] arr = str.split("\\s+");
            double[] contribution = new double[arr.length - 1];
            for (int i = 0; i < contribution.length; i++) {
                contribution[i] = Double.parseDouble(arr[i]);
            }
            double fulfillProbe = Double.parseDouble(arr[arr.length - 1]);

            log.debug(Arrays.toString(contribution));
            log.debug(String.valueOf(fulfillProbe));

            final String[] featureNames = {"合同执行周期", "历史合同执行周期平均值", "计划提报周期", "所在年份计划提报周期平均值", "历史计划提报周期平均值", "分包单价排名", "分包单价方差", "分包单价平均值", "分包单价最大值", "中标单价", "所在年份物料单价方差", "中标总价", "历史物料总价平均值", "历史物料总价方差", "历史物料总价排名", "所在年份物料总价排名", "物料数量", "历史物料数量方差", "历史物料数量最大值", "所在年份物料数量排名", "历史履约率", "履约次数", "历史违约率", "历史违约次数", "历史违约金额最大值", "历史违约金额总和", "历史违约金额平均值", "历史处罚次数", "历史处罚期限总和", "最早供应距今时长", "司法协助次数", "土地抵押面积", "欠税余额", "法律诉讼总数", "法律诉讼案件金额总数", "异常经营次数"};

            Map<String, Double> contributions = new HashMap<>(36);
            for (int i = 0; i < featureNames.length; i++) {
                contributions.put(featureNames[i], contribution[i]);
            }
            log.debug(contributions.toString());
        }

    }


    public static void main(String[] args) {
        String str = "[-3.64143923e-02  2.68205982e-02  0.00000000e+00 -2.09221050e-01 -1.70354056e-03 -1.22057416e-01  4.89423424e-02 -1.40995458e-02 -1.85813569e-02  5.47178537e-02 -1.68017551e-01 -1.30950958e-01 -1.56201228e-01  1.04628921e-01  1.10799782e-02  1.69581585e-02  4.50058877e-02 -5.64839728e-02 -1.22473828e-01  8.00207183e-02 -1.41648996e+00 -4.56264615e-03  7.62387216e-01  5.58218099e-02  1.17687017e-01 -9.65138972e-02 -2.24994887e-02 -5.10225371e-02 -8.64334963e-03 -4.95590977e-02 -3.19335447e-03  0.00000000e+00 -8.70266649e-06  5.83255999e-02  2.32071616e-03  0.00000000e+00]0.7619208";
        predictRiskAndContributions(str);
    }


}
