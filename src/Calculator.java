import org.jfree.data.xy.XYSeries;

import java.util.function.BiFunction;


public class Calculator {

	private double timeInit = 0;
	private double timeEnd = 50;
	private double delta = 0.01;
	private double aParam;
	private double bParam;
	private BiFunction<Double, Double, Double> xInstability = (x, y) -> InstabilityModel.xInstability(x, y);
	private BiFunction<Double, Double, Double> yInstability = (x, y) -> InstabilityModel.yInstability(x, y);
	
	public Calculator(double timeSpan, double delta, double aParam, double bParam) {
		this.timeEnd = timeSpan;
		this.delta = delta;
		if (aParam <= 0 || bParam <= 0)
			throw new IllegalArgumentException("Parameters should be positive real numbers!");
		this.aParam = aParam;
		this.bParam = bParam;
	}

	public Calculator(double aParam, double bParam){
		if (aParam <= 0 || bParam <= 0)
			throw new IllegalArgumentException("Parameters should be positive real numbers!");
		this.aParam = aParam;
		this.bParam = bParam;
	}

	private void setXInstability(BiFunction<Double, Double, Double> func){
		this.xInstability = func;
	}

	private void setYInstability(BiFunction<Double, Double, Double> func){
		this.yInstability = func;
	}

	private BiFunction<Double, Double, Double> makeXAsOfTime() {
		return (x, y) -> 1-(bParam+1)*x+aParam*x*x*y;
	}

	private BiFunction<Double, Double, Double> makeYAsOfTime() {
		return (x, y) -> bParam*x-aParam*x*x*y;
	}
	
	public XYSeries BuildGraphRK(double initX, double initY, String name)
	throws IllegalArgumentException{
		double currentX = initX;
		double currentY = initY;
		BiFunction<Double, Double, Double> xAsOfTime = makeXAsOfTime();
		BiFunction<Double, Double, Double> yAsOfTime = makeYAsOfTime();
		XYSeries outputGraph = new XYSeries(name, false);
		outputGraph.add(currentX, currentY);
		for(double time=timeInit; time<timeEnd; time+=delta){
			double k1 = delta * xAsOfTime.apply(currentX, currentY);
			double l1 = delta * yAsOfTime.apply(currentX, currentY);
			double correctedX = currentX + k1/2.0;
			double correctedY = currentY + l1/2.0;
			double k2 = delta * xAsOfTime.apply(correctedX, correctedY);
			double l2 = delta * yAsOfTime.apply(correctedX, correctedY);
			correctedX = currentX + k2/2.0;
			correctedY = currentY + l2/2.0;
			double k3 = delta * xAsOfTime.apply(correctedX, correctedY);
			double l3 = delta * yAsOfTime.apply(correctedX, correctedY);
			correctedX = currentX + k3/2.0;
			correctedY = currentY + l3/2.0;
			double k4 = delta * xAsOfTime.apply(correctedX, correctedY);
			double l4 = delta * yAsOfTime.apply(correctedX, correctedY);

			InstabilityModel.nextRandomParameters();
			double xNext = (currentX + (k1 + k2 + k3 + k4)/6.0) +
					xInstability.apply(currentX, currentY)*Math.sqrt(delta);
			double yNext = (currentY + (l1 + l2 + l3 + l4)/6.0) +
					yInstability.apply(currentX, currentY)*Math.sqrt(delta);

			outputGraph.add(xNext, yNext);
			currentX = xNext;
			currentY = yNext;
		}
		return outputGraph;
	}

}