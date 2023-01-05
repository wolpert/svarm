# State machine

# A match 3 board is a continuous state machine.

[![Alt text](http://www.plantuml.com/plantuml/png/XP11ImCn58JlxrVCSVVW7yY2jWMh5qjX1V4OTnysDEOjUOcB_zvqDoZYePUGp30VCzbeiZ7b3q_BXSZqdFA4dTWuTaIH9xO9vZveZhpAUBIA9_daxiT-u8IZjrySgm5KQpuvpsYs9VoS9-iRRR6N7CQx5axH7AIbcrkyilxIZb46Lhq4_4K7pDvvZvstuLpUrz0hkI0Nz44wCbjLznxG3nAvepSxdEUonFIpIyEfLlz7hejU9FmfktJbCDAc7ENhlW40)](http://www.plantuml.com/plantuml/png/XP11ImCn58JlxrVCSVVW7yY2jWMh5qjX1V4OTnysDEOjUOcB_zvqDoZYePUGp30VCzbeiZ7b3q_BXSZqdFA4dTWuTaIH9xO9vZveZhpAUBIA9_daxiT-u8IZjrySgm5KQpuvpsYs9VoS9-iRRR6N7CQx5axH7AIbcrkyilxIZb46Lhq4_4K7pDvvZvstuLpUrz0hkI0Nz44wCbjLznxG3nAvepSxdEUonFIpIyEfLlz7hejU9FmfktJbCDAc7ENhlW40)
<!---

@startuml
start

:Setup Board;

repeat :Ensure Board Has Move;
repeat :Get Player Move;
  repeat while (Are Tupal(s) Found?) is (No)
-> Yes;
repeat :Process Tuples;
  :Fill Blanks;
repeat while (Are Tupal(s) Found) is (Yes)
-> No;
:Assign Score;
backward:Switch Players;
repeat while (Player Won?) is (No)
->Yes;
end
@enduml

--->