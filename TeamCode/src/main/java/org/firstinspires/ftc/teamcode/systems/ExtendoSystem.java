package org.firstinspires.ftc.teamcode.systems;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.Servo;

public class ExtendoSystem extends AbstractSystem
{
	private final float RETRACTED = 0.81f;
	private final float HALF = 0.65f;
	private final float SAMPLE = 0.65f;
	private final float THIRD_SAMPLE = 0.65f;
	private final float EXTENDED = 0.5f;

	private ExtendoLevel _level = ExtendoLevel.RETRACTED;

	private final Servo extendo;

	public ExtendoSystem(Servo extendo)
	{
		this.extendo = extendo;


	}

	@Override
	public void init()
	{
		_level = ExtendoLevel.RETRACTED;
		extendo.setPosition(RETRACTED);

	}

	public void extend(ExtendoLevel level)
	{
		_level = level;
		switch (level)
		{
			case RETRACTED:
				extendo.setPosition(RETRACTED);
				break;
			case HALF:
				extendo.setPosition(HALF);
				break;
			case SAMPLE:
				extendo.setPosition(SAMPLE);
				break;
			case THIRD_SAMPLE:
				extendo.setPosition(THIRD_SAMPLE);
				break;
			case EXTENDED:
				extendo.setPosition(EXTENDED);
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
		SAMPLE,
		THIRD_SAMPLE,
		EXTENDED
	}
}