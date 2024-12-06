FaderfoxEC4 {
	classvar func = nil;
  classvar type = "Faderfox EC4";
  var <fader;
  var id;
  var addr;
  var data;
  var values;
  var enable = true;
  var physical = true;
  var midinum = 32;
	
  *new {|type|
		^super.new.init(type);
	}

	init {|type|
		var found = "FaderFox EC4 found";
    var server = Server.default;
		var sources;
		(MIDIClient.initialized.not).if({MIDIClient.init()});
		MIDIIn.connectAll;
		sources = MIDIClient.sources;
    values = Array.fill(16, {0.0});
    addr = [nil, nil];
    data = [nil, nil];

		// Check if 16n is in MIDIEndPoints, store uid in global var.
		(
      sources.do{|source|
        if (source.device == "Faderfox EC4") {
          id = source.uid;

          found.postln;
          physical = true;
        };
      };
		);
		
    (
			fader = 16.collect{
				Bus.control(server, 1);
			};

			func.isNil.not.if {
        func.free;
      };

      (physical).if {
        (type == "cc").if {
          func = MIDIFunc.new({|val, num, chan, src|
            enable.if {
              ("***	ec4: " ++ '[ ' ++ (num - midinum) ++ ' ]' ++ 
              "	Value: " ++ '[ ' ++ val ++ ' ]').postln;
            };

            (num.inclusivelyBetween(midinum, midinum+15)).if {
              values[num - midinum] = val / 127;
            }
          }, msgNum: Array.series(16,midinum,1), chan: 0, msgType: \control, srcID: id);
        };

        (type == "nrpn").if {
          func = [
            MIDIFunc.new({|val, num, ch, src|
              (src == id).if {
                data[1] = val; enable.if{ postln(values); };
                values[addr[1]] = ((data[0] * 128) + data[1]) / 16383;
              };
            }, msgNum: [38], chan: 0, msgType: \control, srcID: id),
            MIDIFunc.new({|val, num, ch, src|
              (src == id).if { data[0] = val; }
            }, msgNum: [6], chan: 0, msgType: \control, srcID: id),
            MIDIFunc.new({|val, num, ch, src|
              (src == id).if { addr[1] = val; }
            }, msgNum: [98], chan: 0, msgType: \control, srcID: id),
            MIDIFunc.new({|val, num, ch, src|
              (src == id).if { addr[0] = val; 

              values.do {|val, i|
                fader[i].set(val);
              }
            }
            }, msgNum: [99], chan: 0, msgType: \control, srcID: id);
          ];
        }
      }
    ); 
  }

	enablePost {
		enable = true;
	}
	
	disablePost {
		enable = false;
	}

  faderAt {|faderPosition|
      ^fader[faderPosition];
  }
}
