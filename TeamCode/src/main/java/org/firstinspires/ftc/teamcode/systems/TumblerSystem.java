package org.firstinspires.ftc.teamcode.systems;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class TumblerSystem extends AbstractSystem
{
	private static final float TUMBLER_TRANSFER = 0f;
	private static final float TUMBLER_IDLE = 0.145f;
	private static final float TUMBLER_HOVER = 0.74f;
	private static final float TUMBLER_PICKUP = 0.82f;

	private TumblerDestination _destination = TumblerDestination.IDLE;

	@Expose
	private final Servo tumbler;

	public TumblerSystem(Servo tumbler)
	{
		this.tumbler = tumbler;
	}

	@Override
	public void init()
	{
		_destination = TumblerDestination.IDLE;
		tumbler.setPosition(TUMBLER_IDLE);
	}

	public void setDestination(TumblerDestination destination)
	{
		_destination = destination;
		switch (destination)
		{
			case TRANSFER:
				tumbler.setPosition(TUMBLER_TRANSFER);
				break;
			case IDLE:
				tumbler.setPosition(TUMBLER_IDLE);
				break;
			case HOVER:
				tumbler.setPosition(TUMBLER_HOVER);
				break;
			case PICKUP:
				tumbler.setPosition(TUMBLER_PICKUP);
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
