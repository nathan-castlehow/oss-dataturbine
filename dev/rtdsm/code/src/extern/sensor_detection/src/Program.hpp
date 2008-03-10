#ifndef CDL_PROGRAM_HPP
#define CDL_PROGRAM_HPP

// $Id: Program.hpp 153 2007-09-24 20:10:37Z ljmiller $



#include <vector>
#include <string>
#include <iostream>
#include "String.hpp"



namespace ascdl {

// Forward decl.
class Program;

class Parameter {
    public:
        virtual ~Parameter() {}
        virtual std::string str() const = 0;
        virtual Parameter* clone(void) const = 0;
};

class TwoDigitParameter : public Parameter {
    public:
        TwoDigitParameter(int td_param);				
        virtual std::string str() const;
        virtual TwoDigitParameter* clone(void) const;
    private:
        int td;
};

class FourDigitParameter : public Parameter {
    public:
        FourDigitParameter(int fd_param);
        virtual std::string str() const;
        virtual FourDigitParameter* clone(void) const;
    private:
        int fd;
};

class FloatingPointParameter : public Parameter {
    public:
        FloatingPointParameter(double fp_param);
        virtual std::string str() const;
        virtual FloatingPointParameter* clone(void) const;
    private:
        double fp;	
};

class Instruction {

    public:

        Instruction(int com);
        Instruction(int com, const Parameter *p1);
        Instruction(int com, const Parameter *p1, const Parameter *p2);
        Instruction(int com, const Parameter *p1, const Parameter *p2,
         const Parameter *p3);
        Instruction(int com, const Parameter *p1, const Parameter *p2,
         const Parameter *p3, const Parameter *p4);
        Instruction(int com, const Parameter *p1, const Parameter *p2,
         const Parameter *p3, const Parameter *p4, const Parameter *p5);
        Instruction(int com, const Parameter *p1, const Parameter *p2,
         const Parameter *p3, const Parameter *p4, const Parameter *p5,
         const Parameter *p6);
        // Must cleanup.
        virtual ~Instruction();

        virtual Instruction* clone() const = 0;

        std::string str(char term) const;
	
    protected:
		
        Instruction(const Instruction& p);
		
    private:
        const int command;
        std::vector<const Parameter *> m_params;
};

class P105 : public Instruction {
    public:
        P105(int addr, int com, int port, int loc,
             double mult, double offset);
        P105(const P105& p);
        virtual P105* clone(void) const;	
};

class InstructionSequence {

    friend class Program;
    friend InstructionSequence operator+(const InstructionSequence &is1,
                                         const InstructionSequence &is2);
		
    public:

        InstructionSequence();
        InstructionSequence(const InstructionSequence& is);
        ~InstructionSequence();		
        void operator=(const InstructionSequence& is);

        // This is a convenience routine that creates a P105 instruction and
        // adds it.
        InstructionSequence &p105(int addr, int com, int port, int loc,
                                  double mult, double offset);
    private:
        void copyHelper(const InstructionSequence& is);
        typedef std::vector<Instruction *> seq_t;
        seq_t m_instructions;
};

std::ostream &
operator<<(std::ostream &, const Program &p);

class Program {

        friend std::ostream &operator<<(std::ostream &, const Program &p);

    public:

        Program(int interval1, const InstructionSequence &table1,
         int interval2, const InstructionSequence &table2);

        std::string str() const;

    private:

        std::string stringHelper(char term) const;

        // A hack to get a constant.
        enum { N_TABLES = 2 };
        InstructionSequence m_table[N_TABLES];
        int m_scanRate[N_TABLES];
};



static char Program_id[] = "$Id: Program.hpp 153 2007-09-24 20:10:37Z ljmiller $";



} // namespace ascdl



#define CDL_MAX_PARAMETERS  6

enum ParamType {
    CDL_DT_2,
    CDL_DT_4,
    CDL_DT_FP,
};


typedef struct Cdl_Parameter_s {
    ParamType type;
    union {
        int integer;
        double fp;
    } value;
} Cdl_Parameter;

typedef struct Cdl_Instruction_s {
    int code;
    int n_parameters;
    // XXX - Somewhat wasteful, maybe make it a dynamically
    // allocated array.
    Cdl_Parameter parameters[CDL_MAX_PARAMETERS];
} Cdl_Instruction;

typedef struct Cdl_Program_s {
    int interval;
    Cdl_Instruction *instructions;
    int n_instructions, instructions_capacity;
} Cdl_Program;



void Cdl_Program__ctor(Cdl_Program *const, const int interval);
void Cdl_Program__dtor(Cdl_Program *const);
void Cdl_Program__append_Instruction(Cdl_Program *const p,
 const Cdl_Instruction i);
Cdl_String Cdl_Program__String(const Cdl_Program *const p);

void Cdl_Program__p17(Cdl_Program *const, const int loc);
void Cdl_Program__p70(Cdl_Program *const, const int reps, const int loc);
void Cdl_Program__p78(Cdl_Program *const p, const int res);
void Cdl_Program__p80(Cdl_Program *const p, const int option, const int param);
void Cdl_Program__p86(Cdl_Program *const p, const int com);
void Cdl_Program__p105(Cdl_Program *const p, const int addr, const int command,
 const int port, const int loc, const double multiplier, const double offset);

void Cdl_Instruction__ctor_0(Cdl_Instruction *const i, const int code);
void Cdl_Instruction__ctor_1(Cdl_Instruction *const i, const int code,
 const Cdl_Parameter p1);
void Cdl_Instruction__ctor_2(Cdl_Instruction *const i, const int code,
 const Cdl_Parameter p1,
 const Cdl_Parameter p2);
void Cdl_Instruction__ctor_6(Cdl_Instruction *const i, const int code,
 const Cdl_Parameter p1, const Cdl_Parameter p2, const Cdl_Parameter p3,
 const Cdl_Parameter p4, const Cdl_Parameter p5, const Cdl_Parameter p6);

Cdl_Parameter Cdl_Parameter__make_2(const int);
Cdl_Parameter Cdl_Parameter__make_4(const int);
Cdl_Parameter Cdl_Parameter__make_fp(const double);
Cdl_String Cdl_Parameter__string(const Cdl_Parameter *);



#endif



// vim: set sw=4 sts=4 expandtab ai:
