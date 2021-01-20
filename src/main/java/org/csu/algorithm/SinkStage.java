package org.csu.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.csu.entity.Agent;
import org.csu.type.AgentType;
import org.csu.entity.Task;
import org.csu.type.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class SinkStage {

    // 阈值计算 a
    public static final double a = 0.99;

    // 阈值计算 b
    public static final double b = 0.01;

    // 阈值计算 lambda
    public static final double lambda = 2.25;

    // 沉没成本计算 x
    public static final double X = 0.05;

    // 沉没成本计算 r
    public static final double R = 0.9;

    // 沉没成本计算 sigma
    public static final double SIGMA = 1 - X - R;

    // 固有降低率
    public static final double R_RATE = 0.9;

    // 沉没损失系数
    public static final double SINK_PARAM = 1.1;

    public static void build(List<Agent> agentList,List<Task> taskList){
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
        sinkStage.updateTaskBid(agentList,taskList);
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
            double sink = R * agent.getSinkMax()+ X * cost + SIGMA * agent.getSinkValue();
            sink = Math.sqrt(sink);
            agent.setSinkValue(sink);
        }
    }

    /**
     * 计算沉没成本阶段的任务报价
     * @param agentList
     * @param taskList
     */
    public void updateTaskBid(List<Agent> agentList,List<Task> taskList){

        // 计算定价参数值
        calPtVal(agentList);

        // 获取沉没成本最大值的平均值
        double sinkMaxAvg = agentList.stream()
                .filter(e->AgentType.SinkStage.equals(e.getAgentType()))
                .mapToDouble(Agent::getSinkMax)
                .average()
                .orElse(0D);

        // 获取沉没成本之和的平均值
        double sinkValAvg = agentList.stream()
                .filter(e->AgentType.SinkStage.equals(e.getAgentType()))
                .mapToDouble(Agent::getSinkValue)
                .average()
                .orElse(0D);

        // 求定价参数平均值
        double pt = agentList.stream()
                .filter(e->AgentType.SinkStage.equals(e.getAgentType()))
                .mapToDouble(Agent::getPriceParam)
                .average()
                .orElse(0D);

        // 更新沉没成本报价
        taskList.stream()
                .filter(e->e.getWinner()!=null)
                .forEach(e->{
                    double v = e.getTaskBid()*(1-R_RATE+R_RATE*sinkValAvg/sinkMaxAvg)*pt;
                    e.setTaskBidForSink(v);
                });
    }

    /**
     * 计算定价参数
     * @param agentList
     */
    public void calPtVal(List<Agent> agentList){
        final Random random = new Random();

        // 计算定价参数-
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            if (agent.getPriceParam() == null
                    && AgentType.SinkStage.equals(agent.getAgentType())) {
                List<Double> ch = new ArrayList<>();
                List<Double> cl = new ArrayList<>();
                List<Double> bh = new ArrayList<>();
                List<Double> bl = new ArrayList<>();

                List<Object[]> objList = agent.getCompletedTask();
                for (int j = 0; j < objList.size(); j++) {
                    Object[] obj = objList.get(j);
                    Task task = (Task) obj[0];
                    double b = (Double) obj[1];
                    double c = (Double) obj[2];
                    if (TaskType.Target.equals(task.getTaskType())) {
                        bh.add(b);
                        ch.add(c);
                    } else {
                        bl.add(b);
                        cl.add(c);
                    }
                }

                // 求平均
                double chAvg = ch.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
                double clAvg = cl.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
                double bhAvg = bh.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
                double blAvg = bl.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
                double temp = 1 - R_RATE + R_RATE * (agent.getSinkValue() / agent.getSinkMax());
                double floor = (chAvg + blAvg - clAvg + SINK_PARAM * agent.getSinkValue()) / bhAvg / temp;
                double upper = 1 / temp;
                double pt = floor + random.nextDouble() * (upper - floor);
                agent.setPriceParam(pt);
            }
        }
    }

    public double calEstimate(Agent agent){
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
        }
        competeProfit /= competeNum;
        targetProfit /= targetNum;

        return 0;
    }
}
