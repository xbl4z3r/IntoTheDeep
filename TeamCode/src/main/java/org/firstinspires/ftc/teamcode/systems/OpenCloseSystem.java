package org.firstinspires.ftc.teamcode.systems;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.Servo;

public class OpenCloseSystem extends AbstractSystem
{
	private static float OPEN = 0;
	private static float CLOSED = 1;

	@Expose
	private final Servo claw;

	public OpenCloseSystem(Servo claw)
	{
		this(claw, OPEN, CLOSED);
	}

	public OpenCloseSystem(Servo claw, float open, float closed)
	{
		this.claw = claw;
		OPEN = open;
		CLOSED = closed;
	}

	@Override
	public void init()
	{
		claw.setPosition(OPEN);
	}

	public void open()
	{
		claw.setPosition(OPEN);
	}

	public void close()
	{
		claw.setPosition(CLOSED);
	}

	public Action openAction()
	{
		return new InstantAction(this::open);
	}

	public Action closeAction()
	{
		return new InstantAction(this::close);
	}
}
