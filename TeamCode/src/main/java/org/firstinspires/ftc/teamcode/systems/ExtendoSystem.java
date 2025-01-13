package org.firstinspires.ftc.teamcode.systems;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class ExtendoSystem
{
	private static final float EXTENDO_RETRACTED = 0;
	private static final float EXTENDO_EXTENDED = 1;

	private ExtendoLevel _level = ExtendoLevel.RETRACTED;

	private final Servo extendoServo1, extendoServo2;

	public ExtendoSystem(HardwareMap hardwareMap)
	{
		extendoServo1 = hardwareMap.get(Servo.class, "extendo1");
		extendoServo2 = hardwareMap.get(Servo.class, "extendo2");
		extendoServo2.setDirection(Servo.Direction.REVERSE);
	}

	public void init()
	{
		_level = ExtendoLevel.RETRACTED;
		extendoServo1.setPosition(EXTENDO_RETRACTED);
		extendoServo2.setPosition(EXTENDO_RETRACTED);
	}

	public void extend(ExtendoLevel level)
	{
		_level = level;
		switch (level)
		{
			case RETRACTED:
				extendoServo1.setPosition(EXTENDO_RETRACTED);
				extendoServo2.setPosition(EXTENDO_RETRACTED);
				break;
			case HALF:
				extendoServo1.setPosition(EXTENDO_EXTENDED / 2);
				extendoServo2.setPosition(EXTENDO_EXTENDED / 2);
				break;
			case EXTENDED:
				extendoServo1.setPosition(EXTENDO_EXTENDED);
				extendoServo2.setPosition(EXTENDO_EXTENDED);
				break;
		}
	}

	public Action extendAction(ExtendoLevel level)
	{
		return new InstantAction(() -> extend(level));
	}

	public ExtendoLevel getExtendoLevel()
	{
		return _level;
	}

	public enum ExtendoLevel
	{
		RETRACTED,
		HALF,
		EXTENDED
	}
}
