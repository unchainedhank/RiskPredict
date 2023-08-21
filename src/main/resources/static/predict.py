import sys
import xgboost as xgb

import numpy as np
import shap


# for i in data.columns:
#     if i == '公司类型' or '城市':
#         lbl = LabelEncoder()
#         data[i] = lbl.fit_transform(list(data[i].values))
# for i in train.columns:
#     if i == '公司类型' or '城市':
#         lbl = LabelEncoder()
#         train[i] = lbl.fit_transform(list(train[i].values))

def predict_risk(data):
    features = np.array(data)
    # xgb.set_config(verbosity=0)
    model = xgb.Booster(model_file='/app/classes/static/model.json')
    # model = xgb.Booster(model_file='/Users/a3/IdeaProjects/RiskPredict/src/main/resources/static/model.json')
    predict = model.predict(xgb.DMatrix(features))
    explainer = shap.TreeExplainer(model)
    shap_values = explainer.shap_values(features)
    shap_values2 = explainer(features)
    return shap_values, shap_values2, predict


# fpr, tpr, thresholds = metrics.roc_curve(test_y, pred[:, 1], pos_label=1)
# fpr_b, tpr_b, thresholds_b = metrics.roc_curve(test_y, pred_b[:, 1], pos_label=1)
# roc_auc = metrics.auc(fpr, tpr)

if __name__ == '__main__':
    data_list = [eval(sys.argv[1])]
    shap_values, shap_values2, predict = predict_risk(data_list)
    print(shap_values[0])
    print(predict[0])
