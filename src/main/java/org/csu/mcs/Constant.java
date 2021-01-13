package org.csu.mcs;

public class Constant {

    // 经济预算
    public static final double BUDGET = 1000;

    // 参与者人数
    public static final int WORKER_NUM = 50;

    // 感知任务数量
    public static final int TASK_NUM = 100;

    // 任务质量上限
    public static final double TASK_QUALITY_UPPER = 10;

    // 任务质量取值范围(取值 10% *上限 ~ 上限)
    public static final double TASK_QUALITY_PERCENT = 0.1;

    // 参与者成本上限
    public static final double AGENT_COST_UPPER = 20;

    // 参与者成本取值范围(取值 30% *上限 ~ 上限 )
    public static final double AGENT_COST_PERCENT = 0.3;

    // 任务质量与价值转化参数
    public static final double TRANSFER_BID = 0.9;

    // 任务质量与报价转化参数
    public static final double TRANSFER_VALUE = 0.9;

    // 任务价值公式
    public static double getTaskBid(double taskValue){
        return taskValue * TRANSFER_BID;
    }

    // 任务价值公式
    public static double getTaskValue(double taskQuality){
        return taskQuality * TRANSFER_BID;
    }

}
