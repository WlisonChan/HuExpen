package org.csu.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.csu.entity.Agent;
import org.csu.type.AgentType;
import org.csu.entity.Task;
import org.csu.type.TaskType;

import java.util.List;

@Slf4j
public class SinkStage {

    // 阈值计算 a
    public static final double a = 0.99;

    // 阈值计算 b
    public static final double b = 0.01;

    // 阈值计算 lambda
    public static final double lambda = 2.25;

    // 沉没成本计算 x
    public static final double x = 0.05;

    // 沉没成本计算 r
    public static final double r = 0.9;

    // 沉没成本计算 sigma
    public static final double sigma = 1 - x - r;

    public static void build(List<Agent> agentList){
        SinkStage sinkStage = new SinkStage();
        // 计算沉没阈值
        sinkStage.calSinkThreshold(agentList);
        // 计算沉没成本值
        agentList.stream().forEach(sinkStage::calSinkSum);
        // 判断是否进入沉没阶段
        agentList.stream().forEach(e->{
            if (e.getSinkThreshold()!=null){
                System.out.println(e.getSinkValue()+" "+e.getSinkThreshold());
            }
            if (e.getSinkThreshold()!=null && e.getSinkValue() >= e.getSinkThreshold()){
                log.info("agent [{}] enter sink stage",e.getAgentId());
                e.setAgentType(AgentType.SinkStage);
            }
        });
    }

    /**
     * 沉没阈值计算
     *
     * @param agentList
     */
    public void calSinkThreshold(List<Agent> agentList) {
        agentList.forEach(e -> {
            int judge = judgeTaskType(e);
            if (1 == judge) {
                double v = calTargetSinkVal(e);
                e.setSinkThreshold(v);
            } else if (0 == judge) {
                double v = calMixSinkVal(e);
                e.setSinkThreshold(v);
            } else if (-1 == judge) {
                // todo
            }
        });
    }

    /**
     * 判断参与者的任务类型
     *
     * @param agent
     * @return
     */
    public int judgeTaskType(Agent agent) {
        List<Object[]> taskList = agent.getCompletedTask();
        if (taskList.size() == 0) {
            // 无任务的参与者
            return -2;
        }
        boolean target = false;
        boolean compete = false;
        for (int i = 0; i < taskList.size(); i++) {
            Task task = (Task) taskList.get(i)[0];
            if (TaskType.Target.equals(task.getTaskType())) {
                target = true;
            } else {
                compete = true;
            }
        }
        if (target && !compete) {
            // 只有目标任务的参与者
            return 1;
        } else if (!target && compete) {
            // 只有竞争任务的参与者
            return -1;
        } else {
            // 两种任务都有
            return 0;
        }
    }

    /**
     * 对只选择目标任务的参与者,计算沉没阈值
     *
     * @param agent
     * @return
     */
    public double calTargetSinkVal(Agent agent) {
        List<Object[]> completedTask = agent.getCompletedTask();
        double profitAvg = 0;
        for (int i = 0; i < completedTask.size(); i++) {
            Object[] obj = completedTask.get(i);
            Task task = (Task) obj[0];
            double val = task.getTaskBid();
            profitAvg += val - agent.calCostForTask(task);
        }
        profitAvg = profitAvg / completedTask.size();
        double val = agent.getVThreshold() - Math.pow(profitAvg, a);
        double res = Math.pow(val, 1.0 / a);
        return res;
    }

    /**
     * 对选择目标任务和竞争任务的参与者,计算沉没阈值
     *
     * @param agent
     * @return
     */
    public double calMixSinkVal(Agent agent) {
        List<Object[]> completedTask = agent.getCompletedTask();
        double competeProfit = 0;
        double targetProfit = 0;
        double competeNum = 0;
        double targetNum = 0;
        for (int i = 0; i < completedTask.size(); i++) {
            Object[] obj = completedTask.get(i);
            Task task = (Task) obj[0];
            double val = task.getTaskBid();
            if (TaskType.Compete.equals(task.getTaskType())) {
                competeNum++;
                competeProfit += val - agent.calCostForTask(task);
            } else {
                targetNum++;
                targetProfit += val - agent.calCostForTask(task);
            }
/*            System.out.println(val+" "+agent.calCostForTask(task));
            System.out.println("参与意愿:\t"+agent.getWillingness());
            System.out.println("感知成本:\t"+agent.getSensoryCost());
            System.out.println("报价:\t"+task.getTaskBid());
            System.out.println("质量:\t"+task.getTaskQuality());*/
        }
        //System.out.println(competeProfit+" "+competeNum);
        competeProfit /= competeNum;
        targetProfit /= targetNum;
        //System.out.println(competeProfit+" "+targetProfit);
        double profit = agent.getVThreshold() - Math.pow(targetProfit, a) + lambda * Math.pow(competeProfit, b);
        double res = Math.pow(profit, 1 / a);
        return res;
    }

    /**
     * 计算此轮沉没成本并更新
     * @param agent
     */
    public void calSinkSum(Agent agent){
        List<Object[]> taskSet = agent.getCompletedTask();
        for (int i = 0; i < taskSet.size(); i++) {
            Object[] obj = taskSet.get(i);
            double cost = (Double)obj[2];
            double sink = r * agent.getSinkMax()+ x * cost + sigma * agent.getSinkValue();
            sink = Math.sqrt(sink);
            agent.setSinkValue(sink);
        }
    }


}
