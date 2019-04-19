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
    static double input;
    static double input1;
    static int input2;
    static int key;
    static int octave;
    static int note;
    static String noteName;
    static int velocity;
    static int recordedTick = 0;
    static boolean yes = true;

    public static void main(String[] args) throws Exception {
        if (args[0].equals("Help") || args[0].equals("help")) {
            System.out.println("There are three parameters needed to be put into the program.");
            System.out.println("The first parameter adjusts the extent of how large the randomization can be for the note off.");
            System.out.println("This must be a double between (not including) 0 and 5.");
            System.out.println("If you put in a value less than or equal to 0, a 0.1 will be used. If you put in a value more than 10, a 10 will be used.");
            System.out.println("The second parameter adjusts the extent of how large the randomization can be for when the notes are played.");
            System.out.println("This must be a double between (not including) 0 and 2.");
            System.out.println("If you put in a value less than or equal to 0, a 0.1 will be used. If you put in a value more than 2, a 2 will be used.");
            System.out.println("The third parameter adjusts the extent of how large the randomization can be for how loud or how soft the notes are played.");
            System.out.println("This must be an int between (not including) 0 and 90.");
            System.exit(0);
        }
        String noteOffParameter = args[0];
        input = Double.parseDouble(noteOffParameter);
        if (input <= 0) {
            input = 0.1;
        } else if (input > 5) {
            input = 5;
        }
        String noteOnParameter = args[1];
        input1 = Double.parseDouble(noteOnParameter);
        if (input1 <= 0) {
            input1 = 0.1;
        } else if (input1 > 2) {
            input1 = 2;
        }
        String velocityParameter = args[2];
        input2 = Integer.parseInt(velocityParameter);
        if (input2 <= 0) {
            input2 = 1;
        } else if (input2 > 90) {
            input2 = 90;
        }
        //Picks which MIDI file to read into the program
        Sequence sequence = MidiSystem.getSequence(new File("Exercise2.mid"));
        Sequence sequence1 = new Sequence(Sequence.SMPTE_30, 32);
        //Location of outputted MIDI file
        File outputFile = new File(System.getProperty("user.home")
                + "//Desktop//file.mid");
        Track trackLoad = sequence1.createTrack();
        int trackNumber = 0;
        for (Track track : sequence.getTracks()) {
            trackNumber++;
            System.out.println("Track " + trackNumber + ": size = " + track.size());
            System.out.println();
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                //event.setTick(event.getTick());
                System.out.print("@" + event.getTick() + " ");
                MidiMessage message = event.getMessage();
                //Outputs the values of the Inputted MIDI file
                outputMidiValues(message);

                //Changes the values of the inputted MIDI file
                //and inserts the values into a new MIDI file to be outputted
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    int key = 0;
                    int velocity = 0;
                    if (sm.getCommand() == NOTE_ON) {
                        key = sm.getData1();
                        velocity = sm.getData2();
                    }
                    //Sets a certain velocity for these specific keys
                    if (key == 0) {
                        velocity = 40;
                    }
                    //Sets a certain velocity for these specific keys
                    if (key == 60) {
                        velocity = 76;
                    }
                    if (key == 128) {
                        velocity = 116;
                    }
                    //Randomizes the velocity of the inputted MIDI file
                    int changeVel = randomizeHighHighVelocity(key);
                    int inverseChangevel = randomizeHighLowVelocity(key);
                    //Randomizes the tick of the inputted MIDI file
                    int tick = randomizeTick((int)event.getTick());
                    //Randomly chooses between changeVel or inverseChangevel for changing the velocity
                    if (tick - recordedTick > 4500) {
                        recordedTick = tick;
                        yes = !yes;
                    }
                    if (yes && velocity > 0) {
                        //Adds the note onto the MIDI file to be outputted
                        trackLoad.add(createNoteEvent(ShortMessage.NOTE_ON, sm.getData1(), (changeVel),
                                tick));
                    } else if (!yes && velocity > 0) {
                        trackLoad.add(createNoteEvent(ShortMessage.NOTE_ON, sm.getData1(), (inverseChangevel),
                                tick));
                    }
                    //Adds moments of rest to the song in the outputted MIDI file
                    trackLoad.add(createNoteEvent(ShortMessage.NOTE_OFF, sm.getData1(), velocity,
                            event.getTick() + (((int) (Math.random() * 201 * input)))));
                    System.out.print("@" + tick + " ");
                    System.out.print("Channel: " + sm.getChannel() + " ");
                    //Outputs the values of the new MIDI file created
                    outputMidiValues(key, changeVel, inverseChangevel, velocity, yes);
                } else {
                    System.out.println(message.toString());
                    trackLoad.add(event);
                }
                try {
                    MidiSystem.write(sequence1, 0, outputFile);        //Writes the new MIDI file to be outputted
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

    private static int randomizeTick(int tick) {
        int randomizedTick;
        if (tick > 40) {
            randomizedTick = (tick + ((-40) + ((int) (Math.random() * 81 * input1))));
        } else {
            randomizedTick = (int)(tick * input1);
        }
        return randomizedTick;
    }

    private static int randomizeHighHighVelocity(int key) {
        int randomizedVelocity;
        //double coefficientVel = 0.90
        double coefficientVel = (100 - input2) * 0.01;
        //int offset = 10;
        int offset = input2;
        //The larger the key, the larger the velocity of the outputted MIDI file
        randomizedVelocity = (int) (key * coefficientVel + offset);
        return randomizedVelocity;
    }

    private static int randomizeHighLowVelocity(int key) {
        int randomizedVelocity;
        //double coefficientVel = 0.90
        double coefficientVel = (100 - input2) * 0.01;
        //int offset = 10;
        int offset = input2;
        randomizedVelocity = (int) ((127 - key) * coefficientVel + offset);
        return randomizedVelocity;
    }

    private static void outputMidiValues(MidiMessage message) {
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            velocity = sm.getData2();
            System.out.print("Channel: " + sm.getChannel() + " ");
            if (sm.getCommand() == NOTE_ON && velocity > 0) {
                key = sm.getData1();
                octave = (key / 12) - 1;
                note = key % 12;
                noteName = NOTE_NAMES[note];
                System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
            } else if (sm.getCommand() == NOTE_OFF || velocity == 0) {
                int key = sm.getData1();
                int octave = (key / 12) - 1;
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
    }

    private static void outputMidiValues(int key, int changeVel, int inverseChangevel, int velocity, boolean yes) {
        if (velocity > 0) {
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
        } else if (velocity == 0) {
            //int key = sm.getData1();
            int octave = (key / 12) - 1;
            int note = key % 12;
            String noteName = NOTE_NAMES[note];
            System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
        } /*else {
            System.out.println("Command:" + sm.getCommand());
        }*/
    }
}
