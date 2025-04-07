package org.team4639.lib.led.subsystem;

import java.util.Objects;
import org.team4639.lib.annotation.PackagePrivate;
import org.team4639.lib.led.pattern.LEDPattern;
import org.team4639.lib.subsystem.Updatable;

public class DummyLEDStrip extends LEDStrip implements Updatable {

  private static volatile LEDStrip instance;

  @PackagePrivate
  static synchronized LEDStrip getInstance() {
    return Objects.requireNonNullElseGet(instance, () -> instance = new DummyLEDStrip());
  }

  @Override
  public void setPattern(LEDPattern pattern) {}

  @Override
  public void doResetTime() {}

  @Override
  public void update() {}
}
