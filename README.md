# Faderfox EC4
Supercollider class for interacting with Faderfox EC4

Allows for controlling parameters with 14bit NRPN and 7bit CC values.

```supercollider
f = FaderfoxEC4.new("nrpn"); // "cc" is also valid option.

SynthDef(\test, {
    var sig, env, verb;
    sig = (SinOsc.ar(\freq.kr(300)) * \vol.kr(0))!2;
    env = EnvGen.kr(
        Env([0,1,0], [\atk.kr(0.1) / 2, \rel.kr(1) * 2], [-4, 4]),
        \trig.kr(1), doneAction: 2
    );
    verb = NHHall.ar(sig) * \verbAmount.kr(0);
    Out.ar(0, (sig + verb) * env );
}).add;

// IT IS SUPERIMPORTANT TO USE '.asMap'.
// otherwise we use the underlying control `Bus` number as a value, 
// and it can get loud.
Synth(\test, [\vol, f.faderAt(0).asMap, \verb, f.faderAt(1)]);

fork{
    var s;
    loop{
        s = Synth(\test, [
            \vol, f.faderAt(0).asMap,
            \verbAmount, f.faderAt(1).asMap,
            \atk, f.faderAt(2).asMap,
            \rel, f.faderAt(3).asMap,
        ]);
    // EXCEPT HERE - where we sample the value once every
    // run through the loop
        wait(f.faderAt(4).getSynchronous.linexp(0, 1, 0.1, 2));
    }
}
```
