package net.runelite.client.automation.input;

public enum KeyboardKey {
    KEY_W("W", 38),
    KEY_A("A", 37),
    KEY_S("S", 40),
    KEY_D("D", 39);

    private final String key;
    private final int keyCode;

    KeyboardKey(String key, int keyCode) {
        this.key = key;
        this.keyCode = keyCode;
    }

    public int getKeyCode() { return this.keyCode; }
    public String toString() { return "KEY_" + this.key; }
}
