/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.automation;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.client.automation.input.Keyboard;
import net.runelite.client.automation.input.KeyboardKey;
import net.runelite.client.automation.util.BotExecutor;
import net.runelite.client.automation.util.Execution;

import java.util.concurrent.ExecutorService;

@Slf4j
public class Camera
{
	@Inject
	private static Client client;

	private static ExecutorService executor = BotExecutor.getExecutor();

	/**
	 * Gets the current yaw value as an angle.
	 *
	 * @return The camera yaw as an angle, this is an angle between 0-360.
	 */
	public static int getYaw() {
		return (int) (client.getCameraYaw() / Constants.MAX_CAMERA_YAW * 360);
	}

	/**
	 * Turns the camera to the specified yaw.
	 *
	 * @param yaw The yaw angle to turn to, this should be an angle between 0-360.
	 */
	public static void turnTo(int yaw) {
		turnTo(yaw, Constants.DEFAULT_CAMERA_TOLERANCE);
	}

	/**
	 * Turns the camera to the specified yaw using a set tolerance.
	 *
	 * @param yaw The yaw angle to turn to, this should be an angle between 0-360.
	 * @param tolerance The tolerance used to check if we have reached our target yaw, a
	 *                  value of 0.05 gives us a 5% tolerance, if the provided tolerance is
	 *                  below 1.5% (0.015) it will default the a 1.5% tolerance.
	 */
	public static void turnTo(int yaw, double tolerance) {

		if (yaw < 0 || yaw > 360) {
			log.info(String.format("[CAMERA] Your target yaw (%d) must be between 0 and 360.", yaw));
			return;
		}

		// Ensure there is always at least a 1.5% tolerance.
		double cameraTolerance = Math.max(Constants.DEFAULT_CAMERA_TOLERANCE, tolerance);

		executor.submit(() -> {

			boolean shouldTurnLeft = shouldTurnLeft(getYaw(), yaw);
			boolean needsToChangeYaw = getAngleDifference(getYaw(), yaw) > (360 * cameraTolerance);

			KeyboardKey key = shouldTurnLeft
					? KeyboardKey.KEY_D
					: KeyboardKey.KEY_A;

			if (needsToChangeYaw) {
				log.info(
					String.format("[CAMERA] Changing yaw to %d, turning %s by pressing %s.",
						yaw,
						shouldTurnLeft
								? "left"
								: "right",
						key.toString()
					)
				);

				Keyboard.pressKey(key);
			}

			while (needsToChangeYaw) {
				needsToChangeYaw = getAngleDifference(getYaw(), yaw) > (360 * cameraTolerance);

				if (!needsToChangeYaw) {
					log.info(
						String.format("[CAMERA] Reached yaw of %d, releasing %s. (Target Yaw: %d).",
							getYaw(), key.toString(), yaw
						)
					);

					Keyboard.releaseKey(key);
				}

				Execution.delay(20, 5, 30);
			}

			return true;
		});

	}

	/**
	 * Calculates whether the quickest route to the target yaw is to turn
	 * the camera left.
	 *
	 * @param startYaw The current camera yaw.
	 * @param targetYaw The yaw that we are aiming to turn to.
	 * @return True if the camera should turn left, or false if it should turn right.
	 */
	private static boolean shouldTurnLeft(int startYaw, int targetYaw) {
		return getAngleDifference(startYaw, targetYaw) < 180;
	}

	/**
	 * Calculates the normalised difference between 2 angles.
	 *
	 * @param startAngle This is the starting angle.
	 * @param targetAngle This is the target/end angle.
	 * @return The rotational difference between the 2 angles.
	 */
	private static int getAngleDifference(int startAngle, int targetAngle) {
		return (targetAngle - startAngle + 360) % 360;
	}
}
