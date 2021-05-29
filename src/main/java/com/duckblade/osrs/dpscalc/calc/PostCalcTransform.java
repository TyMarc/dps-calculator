package com.duckblade.osrs.dpscalc.calc;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import static com.duckblade.osrs.dpscalc.calc.AbstractCalc.SECONDS_PER_TICK;

public enum PostCalcTransform
{

	KARILS_SET_EFFECT(EquipmentRequirement.KARIL, (input, result) ->
	{
		if (!EquipmentRequirement.AMULET_DAMNED.isSatisfied(input))
			return result;

		// 0.75 * dps + 0.25 * (1.5 * dps) = 1.125 * dps
		return result.withDps(result.getDps() * 1.125f);
	}),
	VERACS_SET_EFFECT(EquipmentRequirement.VERACS, (input, result) ->
	{
		float oldDps = result.getDps();

		// ignores hit chance and adds one to damage
		float weaponSpeed = input.getEquipmentStats().getSpeed();
		float veracDps = ((result.getMaxHit() / 2f) + 1) / (weaponSpeed * SECONDS_PER_TICK);
		float combinedDps = oldDps * 0.75f + veracDps * 0.25f;

		return result.withDps(combinedDps);
	}),
	;

	private final BiFunction<CalcInput, CalcResult, CalcResult> mapper;

	PostCalcTransform(BiFunction<CalcInput, CalcResult, CalcResult> mapper)
	{
		this.mapper = mapper;
	}

	PostCalcTransform(BiPredicate<CalcInput, CalcResult> filter, BiFunction<CalcInput, CalcResult, CalcResult> mapper)
	{
		this((i, r) -> filter.test(i, r) ? mapper.apply(i, r) : r);
	}

	PostCalcTransform(EquipmentRequirement filter, BiFunction<CalcInput, CalcResult, CalcResult> mapper)
	{
		this((i, r) -> filter.isSatisfied(i), mapper);
	}

	public static CalcResult processAll(CalcInput input, CalcResult intermediaryResult)
	{
		for (PostCalcTransform transform : values())
		{
			intermediaryResult = transform.mapper.apply(input, intermediaryResult);
		}

		return intermediaryResult;
	}

}