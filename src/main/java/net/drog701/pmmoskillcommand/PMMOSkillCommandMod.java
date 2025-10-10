package net.drog701.pmmoskillcommand;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod("pmmoskillcommand")
public class PMMOSkillCommandMod {
    public PMMOSkillCommandMod() {
        NeoForge.EVENT_BUS.register(new PMMOSkillCommandTickHandler());
    }
}