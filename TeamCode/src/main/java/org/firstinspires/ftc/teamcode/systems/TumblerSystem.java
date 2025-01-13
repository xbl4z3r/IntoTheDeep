package org.firstinspires.ftc.teamcode.systems;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class TumblerSystem extends AbstractSystem
{
	private static final float TUMBLER_TRANSFER = 0f;
	private static final float TUMBLER_IDLE = 0.145f;
	private static final float TUMBLER_HOVER = 0.74f;
	private static final float TUMBLER_PICKUP = 0.82f;

	private TumblerDestination _destination = TumblerDestination.IDLE;

	private final Servo tumblerServo;

	public TumblerSystem(HardwareMap hardwareMap)
	{
		tumblerServo = hardwareMap.get(Servo.class, "tumbler");
	}

	@Override
	public void init()
	{
		_destination = TumblerDestination.IDLE;
		tumblerServo.setPosition(TUMBLER_IDLE);
	}

	public void setDestination(TumblerDestination destination)
	{
		_destination = destination;
		switch (destination)
		{
			case TRANSFER:
				tumblerServo.setPosition(TUMBLER_TRANSFER);
				break;
			case IDLE:
				tumblerServo.setPosition(TUMBLER_IDLE);
				break;
			case HOVER:
				tumblerServo.setPosition(TUMBLER_HOVER);
				break;
			case PICKUP:
				tumblerServo.setPosition(TUMBLER_PICKUP);
				break;
		}
	}

	public Action setDestinationAction(TumblerDestination destination)
	{
		return new InstantAction(() -> setDestination(destination));
	}

	public TumblerDestination getDestination()
	{
		return _destination;
	}

	public enum TumblerDestination {
		TRANSFER,
		IDLE,
		HOVER,
		PICKUP
	}
}
