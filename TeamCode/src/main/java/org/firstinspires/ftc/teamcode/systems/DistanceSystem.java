package org.firstinspires.ftc.teamcode.systems;

import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;

import com.acmerobotics.roadrunner.Action;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class DistanceSystem extends AbstractSystem
{
	private Rev2mDistanceSensor left, right;

	public DistanceSystem(Rev2mDistanceSensor left, Rev2mDistanceSensor right)
	{
		this.left = left;
		this.right = right;
	}

	@Override
	public void init() {}

	public boolean StatusOk()
	{
		return !(left.didTimeoutOccur() && left.didTimeoutOccur());
	}

	public double GetLeftDistanceCM()
	{
		return left.getDistance(DistanceUnit.CM);
	}

	public double GetRightDistanceCM()
	{
		return right.getDistance(DistanceUnit.CM);
	}

	public Action ApproachAtAngle()
	{
		return RunSequentially(

		);
	}
}