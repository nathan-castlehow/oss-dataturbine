// $Id: Program.cpp 153 2007-09-24 20:10:37Z ljmiller $



#include "Program.hpp"
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <sstream>
#include "String.hpp"

using namespace std;



namespace ascdl {

ostream &
operator<<(ostream &os, const Program &p) {
    os << p.stringHelper('\n');
    os.flush();
    return os;
}



//---------------------Instruction Class--------------------------------------------------------------

Instruction::Instruction(int com) : command(com) { }
	
Instruction::Instruction(int com, const Parameter *p1) : command(com) {
	
    m_params.push_back(p1);
}

Instruction::Instruction(int com, const Parameter *p1, const Parameter *p2)
 : command(com) {
	
    m_params.push_back(p1);
    m_params.push_back(p2);
}

Instruction::Instruction(int com, const Parameter *p1, const Parameter *p2,
 const Parameter *p3)
 : command(com) {
	
    m_params.push_back(p1);
    m_params.push_back(p2);
    m_params.push_back(p3);
}
Instruction::Instruction(int com, const Parameter *p1, const Parameter *p2,
 const Parameter *p3, const Parameter *p4)
 : command(com) {
	
    m_params.push_back(p1);
    m_params.push_back(p2);
    m_params.push_back(p3);
    m_params.push_back(p4);
}
Instruction::Instruction(int com, const Parameter *p1, const Parameter *p2,
 const Parameter *p3, const Parameter *p4, const Parameter *p5)
 : command(com) {
	
    m_params.push_back(p1);
    m_params.push_back(p2);
    m_params.push_back(p3);
    m_params.push_back(p4);
    m_params.push_back(p5);
}
Instruction::Instruction(int com, const Parameter *p1, const Parameter *p2,
 const Parameter *p3, const Parameter *p4, const Parameter *p5,
 const Parameter *p6)
 : command(com) {
	
    m_params.push_back(p1);
    m_params.push_back(p2);
    m_params.push_back(p3);
    m_params.push_back(p4);
    m_params.push_back(p5);
    m_params.push_back(p6);		 
}

// Copy constructor

Instruction::Instruction(const Instruction& p) : command(p.command) {
	
    for (unsigned int i = 0; i < p.m_params.size(); i++) {
        m_params.push_back(p.m_params.at(i)->clone());
    }
}

// str function

string
Instruction::str(char term) const {
	
    stringstream str;
    
    str << "P";
    str << command << term;
            
    for(unsigned int i = 0; i < m_params.size(); i++) {
        str << i+1 ;
        str << ":";			
        str << m_params[i]->str();
        str << term;
    }
    
    return str.str();
}

Instruction::~Instruction() {	
    for(unsigned int i = 0; i < m_params.size(); i++) {		
        delete m_params.at(i);		
    }
}

//---------------------TwoDigitParameter Class--------------------------------------------------------------

TwoDigitParameter::TwoDigitParameter(int td_param) : td(td_param) {
}

string
TwoDigitParameter::str() const {
	
	stringstream str;
	str << td;
	return str.str();	
}

TwoDigitParameter*
TwoDigitParameter::clone(void) const {
	return (new TwoDigitParameter(*this));
}

//---------------------FourDigitParameter Class--------------------------------------------------------------
FourDigitParameter::FourDigitParameter(int fd_param) : fd(fd_param) {
}

string
FourDigitParameter::str() const {
	
	stringstream str;
	str << fd;
	return str.str();	
}

FourDigitParameter*
FourDigitParameter::clone(void) const {
	return new FourDigitParameter(*this);
}

//---------------------FloatingPointParameter Class--------------------------------------------------------------
FloatingPointParameter::FloatingPointParameter(double fp_param) : fp(fp_param) {
}

string
FloatingPointParameter::str(void) const{
	
	stringstream str;
	str << fp;
	return str.str();	
}

FloatingPointParameter*
FloatingPointParameter::clone(void) const {
	return new FloatingPointParameter(*this);
}

//---------------------P105 Class--------------------------------------------------------------

//Constructor for the P105 instruction
P105::P105(int addr, int com, int port, int loc,
         double mult, double offset) : Instruction (105,
		                       new FourDigitParameter(addr),
             		           new TwoDigitParameter(com),
							   new TwoDigitParameter(port),
							   new FourDigitParameter(loc),
							   new FloatingPointParameter(mult),
							   new FloatingPointParameter(offset)) {
}

P105::P105(const P105& p) : Instruction(p) {
}

P105*
P105::clone(void) const {
    return new P105(*this);
}

//---------------------InstructionSequence Class--------------------------------------------------------------

InstructionSequence&
InstructionSequence::p105(int addr, int com, int port, int loc,
         double mult, double offset) {
		 
    P105* p = new P105(addr,com,port,loc,mult,offset);
    m_instructions.push_back(p); 

    return *this;
}

void
InstructionSequence::copyHelper(const InstructionSequence& is) {
	for (unsigned int i = 0; i < is.m_instructions.size(); i++) {
		m_instructions.push_back(is.m_instructions[i]->clone());
	}
}

InstructionSequence::InstructionSequence(const InstructionSequence& is) {	
	copyHelper(is);
}

InstructionSequence::~InstructionSequence() {
    for(unsigned int i = 0; i < m_instructions.size(); i++) {
        delete m_instructions[i];
    }	
}

//InstructionSequence::~InstructionSequence() {}

InstructionSequence::InstructionSequence() {
}

void
InstructionSequence::operator=(const InstructionSequence& is) {

 	for (unsigned int i = 0; i < m_instructions.size(); i++) {		
		delete m_instructions[i];
	}
	m_instructions.clear();	
	copyHelper(is);
}

InstructionSequence
operator+(const InstructionSequence &is1, 
	      const InstructionSequence &is2) {

	InstructionSequence is;
	
	for (InstructionSequence::seq_t::const_iterator it = is1.m_instructions.begin();
	it != is1.m_instructions.end(); ++it) {
	   is.m_instructions.push_back((*it)->clone());
	}
	
	for (InstructionSequence::seq_t::const_iterator it = is2.m_instructions.begin();
	it != is2.m_instructions.end(); ++it) {
	   is.m_instructions.push_back((*it)->clone());
	   
	}
		
	return is;
}


//---------------------Program Class--------------------------------------------------------------
   
string
Program::stringHelper(char term) const {

    stringstream str;
	
    for (int i = 0; i < N_TABLES; i++) {
		
		
        str << "MODE " << i + 1 << term;
        str << "SCAN RATE " << m_scanRate[i] << term;
		
		str << i + 1;
		str << ":";
		
		for (InstructionSequence::seq_t::const_iterator it
         = m_table[i].m_instructions.begin();
         it != m_table[i].m_instructions.end(); ++it) {           
			str << (*it)->str(term);            
        }
    }

    return str.str();
}

Program::Program(int interval1, const InstructionSequence &table1,
         int interval2, const InstructionSequence &table2) {

	m_table[0] = table1;
	m_table[1] = table2;
	
	m_scanRate[0] = interval1;
	m_scanRate[1] = interval2;
		 
}

string
Program::str() const {
	return stringHelper('\n');
}


} // namespace ascdl



void Cdl_Program__ctor(Cdl_Program *const p, const int interval) {
    p->interval = interval;
    p->n_instructions = 0;
    p->instructions_capacity = 50;
    p->instructions = (Cdl_Instruction *) malloc(p->instructions_capacity*sizeof(Cdl_Instruction));
}

void Cdl_Program__dtor(Cdl_Program *p) {
    assert(p->instructions_capacity >= 50);
    assert(p->instructions >= 0);
    free(p->instructions);
}

void
Cdl_Program__p17(Cdl_Program *const p, const int loc) {
    
    Cdl_Instruction i;

    Cdl_Instruction__ctor_1(&i,
        17,
        Cdl_Parameter__make_4(loc)
    );

    Cdl_Program__append_Instruction(p, i);
}

void
Cdl_Program__p70(Cdl_Program *const p, const int reps, const int loc) {
    
    Cdl_Instruction i;

    Cdl_Instruction__ctor_2(&i,
        70,
        Cdl_Parameter__make_4(reps),
        Cdl_Parameter__make_4(loc)
    );

    Cdl_Program__append_Instruction(p, i);
}

void
Cdl_Program__p78(Cdl_Program *const p, const int res) {
    
    Cdl_Instruction i;

    Cdl_Instruction__ctor_1(&i,
        78,
        Cdl_Parameter__make_2(res)
    );

    Cdl_Program__append_Instruction(p, i);
}

void
Cdl_Program__p80(Cdl_Program *const p, const int option, const int param) {
    
    Cdl_Instruction i;

    Cdl_Instruction__ctor_2(&i,
        80,
        Cdl_Parameter__make_2(option),
        Cdl_Parameter__make_4(param)
    );

    Cdl_Program__append_Instruction(p, i);
}

void
Cdl_Program__p86(Cdl_Program *const p, const int com) {
    
    Cdl_Instruction i;

    Cdl_Instruction__ctor_1(&i,
        86,
        Cdl_Parameter__make_2(com)
    );

    Cdl_Program__append_Instruction(p, i);
}

void
Cdl_Program__p105(Cdl_Program *const p,
 const int addr,
 const int command,
 const int port,
 const int loc,
 const double multiplier,
 const double offset) {
    
    Cdl_Instruction i;

    Cdl_Instruction__ctor_6(&i,
        105,
        Cdl_Parameter__make_4(addr),
        Cdl_Parameter__make_2(command),
        Cdl_Parameter__make_2(port),
        Cdl_Parameter__make_4(loc),
        Cdl_Parameter__make_fp(multiplier),
        Cdl_Parameter__make_fp(offset)
    );

    Cdl_Program__append_Instruction(p, i);
}

void
Cdl_Program__append_Instruction(Cdl_Program *const p, const Cdl_Instruction i) {
    
    if (p->n_instructions == p->instructions_capacity) {
        p->instructions_capacity *= 2;
        p->instructions = (Cdl_Instruction *) realloc(p->instructions, p->instructions_capacity);
    }

    assert(p->n_instructions < p->instructions_capacity);

    p->instructions[p->n_instructions++] = i;
}

Cdl_String
Cdl_Program__String(const Cdl_Program *const p) {

    Cdl_String s;
    Cdl_String__ctor(&s);

    Cdl_String__fcatl(&s, "SCAN RATE %d", p->interval);
    Cdl_String__fcatl(&s, "");

    for (int i = 0; i < p->n_instructions; i++) {
        Cdl_Instruction *it = &p->instructions[i];
        Cdl_String__fcatl(&s, "%d:P%d", i + 1, it->code);
        for (int j = 0; j < it->n_parameters; j++) {
            Cdl_String__fcat(&s, "%d:", j + 1);
            Cdl_String s2 = Cdl_Parameter__string(&it->parameters[j]);
            Cdl_String__cat(&s, s2.str);
            Cdl_String__dtor(&s2);
            Cdl_String__fcatl(&s, "");
        }
    }

    return s;
}

void
Cdl_Instruction__ctor_0(Cdl_Instruction *const i, const int code) {
    i->code = code;
    i->n_parameters = 0;
}

void
Cdl_Instruction__ctor_1(Cdl_Instruction *const i, const int code,
 const Cdl_Parameter p1) {
    i->code = code;
    i->n_parameters = 1;
    i->parameters[0] = p1;
}

void
Cdl_Instruction__ctor_2(Cdl_Instruction *const i, const int code,
 const Cdl_Parameter p1,
 const Cdl_Parameter p2) {
    i->code = code;
    i->n_parameters = 2;
    i->parameters[0] = p1;
    i->parameters[1] = p2;
}

void
Cdl_Instruction__ctor_3(Cdl_Instruction *const i, const int code,
 const Cdl_Parameter p1,
 const Cdl_Parameter p2,
 const Cdl_Parameter p3) {
    i->code = code;
    i->n_parameters = 3;
    i->parameters[0] = p1;
    i->parameters[1] = p2;
    i->parameters[2] = p3;
}

void
Cdl_Instruction__ctor_6(Cdl_Instruction *const i, const int code,
 const Cdl_Parameter p1,
 const Cdl_Parameter p2,
 const Cdl_Parameter p3,
 const Cdl_Parameter p4,
 const Cdl_Parameter p5,
 const Cdl_Parameter p6) {
    i->code = code;
    i->n_parameters = 6;
    i->parameters[0] = p1;
    i->parameters[1] = p2;
    i->parameters[2] = p3;
    i->parameters[3] = p4;
    i->parameters[4] = p5;
    i->parameters[5] = p6;
}

Cdl_Parameter
Cdl_Parameter__make_2(int v) {
    Cdl_Parameter p;
    p.type = CDL_DT_2;
    p.value.integer = v;
    return p;
}

Cdl_Parameter
Cdl_Parameter__make_4(int v) {
    Cdl_Parameter p;
    p.type = CDL_DT_4;
    p.value.integer = v;
    return p;
}

Cdl_Parameter
Cdl_Parameter__make_fp(double v) {
    Cdl_Parameter p;
    p.type = CDL_DT_FP;
    p.value.fp = v;
    return p;
}

Cdl_String
Cdl_Parameter__string(const Cdl_Parameter *const p) {
    Cdl_String s;
    Cdl_String__ctor(&s);
    switch (p->type) {
        case CDL_DT_2:
            {
                int v = p->value.integer;
                assert(v > -99 && v < 99);
                Cdl_String__fcat(&s, "%d", v);
            }
            break;
        case CDL_DT_4:
            {
                int v = p->value.integer;
                assert(v > -9999 && v < 9999);
                Cdl_String__fcat(&s, "%d", v);
            }
            break;
        case CDL_DT_FP:
            {
                double v = p->value.fp;
                assert((v >= 1E-19 && v <= 9E18) || (v == 0)
                 || (v >= -9E18 && v <= -1E-19));
                Cdl_String__fcat(&s, "%f", v);
            }
            break;
        default:
            assert(0);
            abort();
    }
    return s;
}



// vim: set sw=4 sts=4 expandtab ai:
