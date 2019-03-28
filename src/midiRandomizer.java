import java.io.File;
import java.io.IOException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.InvalidMidiDataException;
import java.io.BufferedReader;
//import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class midiRandomizer {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static void main(String[] args) throws Exception {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        System.out. print("Input the tune: ");
        String name = reader.readLine();
        double input = Double.parseDouble(name);
        Sequence sequence = MidiSystem.getSequence(new File("Exercise1.mid"));
        Sequence sequence1 = new Sequence(Sequence.SMPTE_30, 32);
        File outputFile = new File(System.getProperty("user.home")
                + "//Desktop//file.mid");
        Track trackLoad = sequence1.createTrack();

        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
            trackNumber++;
            System.out.println("Track " + trackNumber + ": size = " + track.size());
            System.out.println();
            for (int i=0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                event.setTick(event.getTick());
                System.out.print("@" + event.getTick() + " ");
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    System.out.print("Channel: " + sm.getChannel() + " ");
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else {
                        System.out.println("Command:" + sm.getCommand());
                    }
                } else {
                    System.out.println("Other message: " + message.getClass());
                }
                /*try {
                    sequence1 = new Sequence(Sequence.SMPTE_30, 2);


                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                    System.exit(1);
                }*/
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    int key = 0;
                    int velocity = 0;
                    if (sm.getCommand() == NOTE_ON) {
                        key = sm.getData1();
                        velocity = sm.getData2();
                    }
                    //int maxVel = 128;
                    int maxNote = 128;
                    double coefficientVel = 0.90;
                    int offset = 10;
                    int changeVel = (int)(key * coefficientVel + offset);
                    int inverseChangevel = (int)((127 - key) * coefficientVel + offset);
                    if (key == 0) {
                        velocity = 40;
                    }
                    if (key == 60) {
                        velocity = 76;
                    }
                    if (key == maxNote) {
                        velocity = 116;
                    }
                    int tick;
                    if (event.getTick() > 40) {
                        tick = (int)(event.getTick() + ((-40) + ((int)(Math.random() * 81))));
                    } else {
                        tick = (int) event.getTick();
                    }
                    //int tick = ((int)(event.getTick() + 600));
                    //event.setTick(tick);
                    //trackLoad.add(event);
                    Random random = new Random();
                    boolean yes = random.nextBoolean();
                    if (yes && velocity > 0) {
                        trackLoad.add(createNoteEvent(ShortMessage.NOTE_ON, sm.getData1(), (changeVel),
                                tick));
                    } else if (!yes && velocity > 0){
                        trackLoad.add(createNoteEvent(ShortMessage.NOTE_ON, sm.getData1(), (inverseChangevel),
                                tick));
                    }
                    trackLoad.add(createNoteEvent(ShortMessage.NOTE_OFF, sm.getData1(), velocity,
                            event.getTick() + (((int) (Math.random() * 201 * input)))));
                    System.out.print("@" + tick + " ");
                    System.out.print("Channel: " + sm.getChannel() + " ");
                    if (sm.getCommand() == NOTE_ON && velocity > 0) {
                        //int key = sm.getData1();
                        int octave = (key / 12) - 1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        //int velocity = sm.getData2();
                        if (yes) {
                            System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + (changeVel));
                        } else {
                            System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + (inverseChangevel));
                        }
                    } else if (sm.getCommand() == NOTE_OFF || velocity == 0) {
                        //int key = sm.getData1();
                        int octave = (key / 12) - 1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        velocity = sm.getData2();
                        System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else {
                        System.out.println("Command:" + sm.getCommand());
                    }
                } else {
                    System.out.println(message.toString());
                    trackLoad.add(event);
                }
                try {
                    MidiSystem.write(sequence1, 0, outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            System.out.println();
        }




    }

    private static MidiEvent createNoteEvent(int nCommand, int nKey,
                                             int nVelocity, long lTick) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(nCommand, 0, nKey, nVelocity);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            System.exit(1);
        }
        MidiEvent event = new MidiEvent(message, lTick);
        return event;
    }
}
