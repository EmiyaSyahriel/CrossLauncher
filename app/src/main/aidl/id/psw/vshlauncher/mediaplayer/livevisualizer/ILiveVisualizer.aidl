// ILiveVisualizer.aidl
package id.psw.vshlauncher.mediaplayer.livevisualizer;

// Declare any non-default types here with import statements

interface ILiveVisualizer {
    /**
     * Setting current audio spectrum to the service
     */
    void setAudioSpectrum(in float[] spectrumData);

    /**
    * Request processed visualizer image out of service
    */
    void getVisualizerImage(in int width, in int height, in int[] colorData);
}
