// IXMBMusicWaveGLRenderer.aidl
package id.psw.vshlauncher;

// Declare any non-default types here with import statements

interface IXMBMusicWaveGLRenderer {
    /**
    * Set music data
    */
    void setMusicInfo(
        in int itemId,
        in String title,
        in String artist,
        in String album,
        in float duration
    );
    /**
    * Called when viewport size changes
    */
    void setViewportSize(
        in int width,
        in int height
    );
    /**
    * Do render call with the given time and frequency data
    */
    void render(
        in float normalizedTime,
        in float trueTime,
        in float[] freqData
    );
}