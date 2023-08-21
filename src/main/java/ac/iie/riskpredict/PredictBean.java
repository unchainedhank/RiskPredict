package ac.iie.riskpredict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author a3
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredictBean {
    // 合同执行周期
    private Integer contract_cycle;

    // 历史合同执行周期平均值
    private Double avg_history_contract_cycle;

    // 计划提报周期
    private Integer plan_cycle;

    // 所在年份计划提报周期平均值
    private Double avg_yearly_plan_cycle;

    // 历史计划提报周期
    private Integer history_plan_cycle;

    // 分包单价排名
    private Integer rank_pack_unit_price;

    // 分包单价方差
    private Double vari_pack_unit_price;

    // 分包单价平均值
    private Double avg_pack_unit_price;

    // 分包单价最大值
    private Double max_pack_unit_price;

    // 中标单价
    private Double unit_price;

    // 所在年份物料单价方差
    private Double vari_yearly_unit_price;

    // 中标总价
    private Double total_price;

    // 历史物料总价平均值
    private Integer avg_history_total_price;

    // 历史物料总价方差
    private Double vari_history_total_price;

    // 历史物料总价排名
    private Integer rank_history_total_price;

    // 所在年份物料总价排名
    private Integer rank_yearly_total_price;

    // 物料数量
    private Integer supply_num;

    // 历史物料数量方差
    private Double vari_history_num;

    // 历史物料数量最大值
    private Integer max_history_num;

    // 所在年份物料数量排名
    private Integer rank_yearly_num;

    // 历史履约率
    private Double history_fulfill_rate;

    // 履约次数
    private Integer fulfill_times;

    // 历史违约率
    private Double history_breach_rate;

    // 历史违约次数
    private Integer history_breach_times;

    // 历史违约金额最大值
    private Integer max_history_breach_amount;

    // 历史违约金额总和
    private Integer sum_history_breach_amount;

    /**
     * 历史违约金额平均值
     */
    private Integer avg_history_breach_amount;

    /**
     * 原材料类别
     */

    /**
     * 历史处罚次数
     */
    private Integer punish_times;

    /**
     * 历史处罚期限总和
     */
    private Integer sum_punish;


    // 最早供应距今时长
    private Integer supply_history_length;

    // 司法协助
    private Integer legal_assist_times;

    // 土地抵押面积
    private Integer land_mortgage_area;

    // 欠税余额
    private Integer overdue_tax;

    // 法律诉讼总数
    private Integer lawsuit_times;

    // 法律诉讼案件金额总数
    private Integer sum_lawsuit;

    // 异常经营次数
    private Integer abnormal_operation_times;
}
