package org.csu.mcs;

public class Constant {

    // 经济预算
    public static final double BUDGET = 1000;

    // 参与者人数
    public static final int WORKER_NUM = 50;

    // 感知任务数量
    public static final int TASK_NUM = 100;

    // 性能因子
    public static final double PERFORMANCE_FACTOR = 1.0;

    // 任务质量上限
    public static final double TASK_QUALITY_UPPER = 15.0;

    // 任务质量取值范围(取值 10% *上限 ~ 上限)
    public static final double TASK_QUALITY_FLOOR = 5.0;

    // 参与者成本上限
    public static final double AGENT_COST_UPPER = 20;

    // 参与者成本取值下限
    public static final double AGENT_COST_FLOOR = 10;

    // 任务质量与价值转化参数
    public static final double TRANSFER_BID = 1.0;

    // 任务质量与报价转化参数
    public static final double TRANSFER_VALUE = 1.0;

    // 参与者感知成本范围上限
    public static final double SENSOR_COST_UPPER = 2.0;

    // 参与者感知成本范围下限
    public static final double SENSOR_COST_FLOOR = 1.0;

    // 参与者意愿值范围上限
    public static final double WILLINGNESS_UPPER = 0.1;

    // 参与者意愿值范围下限
    public static final double WILLINGNESS_FLOOR = 0.05;

    // 参与者参与阈值范围上限
    public static final double PARTICIPATION_UPPER = 15.0;

    // 参与者参与阈值范围下限
    public static final double PARTICIPATION_FLOOR = 10.0;

    // 参与者沉没成本最大值上限
    public static final double SINK_MAX_UPPER = 20.0;

    // 参与者沉没成本最大值下限
    public static final double SINK_MAX_FLOOR = 10.0;

    // 任务报价公式
    public static double getTaskBid(double taskValue) {
        return taskValue * TRANSFER_VALUE;
    }

    // 任务价值公式
    public static double getTaskValue(double taskQuality) {
        return taskQuality * TRANSFER_BID;
    }

}
