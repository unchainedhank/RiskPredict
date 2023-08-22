import pandas as pd
import xgboost as xgb
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score, roc_curve
from sklearn.model_selection import train_test_split
import numpy as np
from hyperopt import hp, STATUS_OK, Trials, fmin, tpe

def metrics_sklearn(y_valid, y_pred_):
    """模型对验证集和测试集结果的评分"""
    print('model_rate:')
    # 错误率
    print('error: %.2f%%' % ((y_pred_ != test_y).sum() / float(test_y.shape[0])))

    # 准确率
    accuracy = accuracy_score(y_valid, y_pred_)
    print('Accuracy：%.2f%%' % (accuracy * 100))

    # 精准率
    precision = precision_score(y_valid, y_pred_)
    print('Precision：%.2f%%' % (precision * 100))

    # 召回率
    recall = recall_score(y_valid, y_pred_)
    print('Recall：%.2f%%' % (recall * 100))

    # F1值
    f1 = f1_score(y_valid, y_pred_)
    print('F1：%.2f%%' % (f1 * 100))

    # auc曲线下面积
    auc = roc_auc_score(y_valid, y_pred_)
    print('AUC：%.2f%%' % (auc * 100))

    # ks值
    fpr, tpr, thresholds = roc_curve(y_valid, y_pred_)
    ks = max(abs(fpr - tpr))
    print('KS：%.2f%%' % (ks * 100))


# feature_columns = [
#     '合同执行周期',
#     '历史合同执行周期平均值',
#     '计划提报周期',
#     '所在年份计划提报周期平均值',
#     '历史计划提报周期',
#
#     '分包单价排名',
#     '分包单价方差',
#     '分包单价平均值',
#     '分包单价最大值',
#
#     '中标单价',
#     '所在年份物料单价方差',
#
#     '中标总价',
#     '历史物料总价平均值',
#     '历史物料总价方差',
#     '历史物料总价排名',
#     '所在年份物料总价排名',
#
#     '物料数量',
#     '历史物料数量方差',
#     '历史物料数量最大值',
#     '所在年份物料数量排名',
#
#     '历史履约率',
#     '履约次数',
#
#     '历史违约率',
#     '历史违约次数',
#     '历史违约金额最大值',
#     '历史违约金额总和',
#     '历史违约金额平均值',
#
#     '原材料类别',
#
#     '历史处罚次数',
#     '历史处罚期限总和',
#
#     '注册资本',
#     '城市',
#     '最早供应距今时长',
#
#     '司法协助', '土地抵押面积', '欠税余额', '法律诉讼总数', '法律诉讼案件金额总数', '经营异常次数']
feature_columns = ['contract_cycle', 'avg_history_contract_cycle', 'plan_cycle', 'avg_yearly_plan_cycle',
                   'history_plan_cycle', 'rank_pack_unit_price', 'vari_pack_unit_price', 'avg_pack_unit_price',
                   'max_pack_unit_price', 'unit_price', 'vari_yearly_unit_price', 'total_price',
                   'avg_history_total_price', 'vari_history_total_price', 'rank_history_total_price',
                   'rank_yearly_total_price', 'supply_num', 'vari_history_num', 'max_history_num', 'rank_yearly_num',
                   'history_fulfill_rate', 'fulfill_times', 'history_breach_rate', 'history_breach_times',
                   'max_history_breach_amount', 'sum_history_breach_amount', 'avg_history_breach_amount',
                   'punish_times', 'sum_punish',
                   'supply_history_length',
                   'legal_assist_times', 'land_mortgage_area', 'overdue_tax', 'lawsuit_times', 'sum_lawsuit',
                   'abnormal_operation_times']

target_column = 'sample_class'
# data = pd.read_csv('/app/classes/static/t_supply_risk.csv', low_memory=False)
data = pd.read_csv('/Users/a3/IdeaProjects/RiskPredict/src/main/resources/static/t_supply_risk.csv', low_memory=False,error_bad_lines=False)

# 划分数据集
train, test = train_test_split(data)

train_X = train[feature_columns].values
train_y = train[target_column].values
test_X = test[feature_columns].values
test_y = test[target_column].values

# hyperopt自动优化参数
# 搜索空间
space = {'max_depth': hp.choice('max_depth', np.arange(4, 60, 1, dtype=int)),
         'gamma': hp.uniform('gamma', 1, 9),
         'reg_alpha': hp.quniform('reg_alpha', 10, 180, 1),
         'reg_lambda': hp.uniform('reg_lambda', 0, 1),
         'colsample_bytree': hp.quniform('colsample_bytree', 0.5, 1.0, 0.1),
         'min_child_weight': hp.choice('min_child_weight', np.arange(10, 200, 10, dtype=int)),
         'subsample': hp.quniform('subsample', 0.6, 0.9, 0.1),
         'eta': hp.quniform('eta', 0.1, 0.7, 0.1),
         'n_estimators': hp.choice('n_estimators', np.arange(1000, 10000, 10, dtype=int)),
         'scale_pos_weight': hp.quniform('scale_pos_weight', 1, 3, 0.1),
         }


# 搜索目标
def objective(space):
    clf = xgb.XGBClassifier(
        n_estimators=space['n_estimators'], max_depth=int(space['max_depth']), gamma=space['gamma'],
        reg_alpha=int(space['reg_alpha']), min_child_weight=int(space['min_child_weight']),
        colsample_bytree=int(space['colsample_bytree']), booster='gbtree', subsample=space['subsample'],
        eta=space['eta'], scale_pos_weight=space['scale_pos_weight'])

    evaluation = [(train_X, train_y), (test_X, test_y)]

    clf.fit(train_X, train_y,
            eval_set=evaluation, eval_metric="auc",
            early_stopping_rounds=10, verbose=False)

    predict = clf.predict(test_X)
    score = roc_auc_score(test_y, predict > 0.5)
    # print("SCORE:", roc_auc_score)
    return {'loss': -score, 'status': STATUS_OK}


trials = Trials()

best_hyperparams = fmin(fn=objective,
                        space=space,
                        algo=tpe.suggest,
                        max_evals=100,
                        trials=trials)
print("The best hyperparameters are : ", "\n")
print(best_hyperparams)
xgb.set_config(verbosity=0)

# 初始化模型
# model = xgb.XGBClassifier(colsample_bytree=1,
#                           eta=0.6, gamma=2.0424,
#                           max_depth=46,
#                           min_child_weight=2, n_estimators=652,
#                           reg_alpha=29.0, reg_lambda=0.2618,
#                           scale_pos_weight=1, subsample=0.8
#                           )
model = xgb.XGBClassifier(**best_hyperparams)


# 拟合模型
model.fit(train_X, train_y)
#
pred = model.predict(test_X)
metrics_sklearn(test_y, pred)
#
# 保存模型
model.save_model(fname='/Users/a3/IdeaProjects/RiskPredict/src/main/resources/static/model.json')
# model.save_model(fname='/app/classes/static/model.json')



