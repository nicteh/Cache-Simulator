/**
 * Project 2: Cache Simulator
 * Name:		Teh Zi Cong Nicholas
 */

import java.util.*;

public class Cache {
	public static short[] mainMem = new short[2048];
	public static Block[] slots = new Block[16]; // 16 slots of 1 block each

	public Cache() {
		// Intialise main memory
		short putI = 0;
		for (short i = 0; i < mainMem.length; i++) {
			// Reset putI when exceed biggest value
			if (putI == 0xFF + 1) {
				putI = 0;
			}

			mainMem[i] = putI;
			putI++;
		}

		// Initialise slots
		for (short i = 0; i < slots.length; i++) {
			slots[i] = new Block(i);
		}
	}

	private boolean checkCacheHit(Address add) {
		// Go to the slot in question and if valid == 1 and tags match => HIT, else Miss
		Block block = slots[add.slot];
		return (block.isValid() && block.checkTag(add.tag));
	}

	public void read(Address add) {
		Block block = slots[add.slot];
		if (checkCacheHit(add)) { // Cache Hit
			int val = block.getData(add.offset);
			System.out.println("At that byte there is the value " + Integer.toHexString(val) + " (Cache Hit)");
		} else {
			if (block.isDirty()) {
				// Write back to main memory
				writeBack(add);
				block.setDirty(0);
			}

			// Copy data into the slot
			for (short i = 0; i < 16; i++) {
				block.setData(i, mainMem[add.beginAddress + i]);
			}

			block.setValid(1); // Set valid to 1
			block.updateTag(add.tag); // Update tag
			int val = mainMem[add.address]; // Get the data requested
			System.out.println("At that byte there is the value " + Integer.toHexString(val) + " (Cache Miss)");
		}
		return;
	}

	public void writeBack(Address address) {
		Block block = slots[address.slot];
		Address writeBackAddress = new Address((block.tag << 8) + (block.slot << 4));
		for (short i = 0; i < 16; i++) {
			mainMem[writeBackAddress.beginAddress + i] = block.getData(i);
		}
	}

	public void write(Address add, short data) {
		Block block = slots[add.slot];
		if (checkCacheHit(add)) { // If Cache Hit
			block.setDirty(1);
			block.setData(add.offset, data);
			System.out.println("Value " + Integer.toHexString(data) + " has been written to address " + Integer.toHexString(add.address) + " (Cache Hit)");
		} else {
			if (block.isDirty()) {
				// Write back to main memory
				writeBack(add);
				block.setDirty(0);
			}

			// Copy data into the slot
			for (short i = 0; i < 16; i++) {
				block.setData(i, mainMem[add.beginAddress + i]);
			}

			block.setDirty(1);
			block.setValid(1); // Set valid to 1
			block.updateTag(add.tag); // Update tag
			block.setData(add.offset, data);
			System.out.println("Value " + Integer.toHexString(data) + " has been written to address " + Integer.toHexString(add.address) + " (Cache Miss)");
		}
		return;
	}

	public void display() {
		System.out.println("Slot  Valid  Tag     Data");
		for (int i = 0; i < slots.length; i++) {
			slots[i].printBlock();
			System.out.println();
		}
	}

	public static void main(String[] args) {
		Cache simulator = new Cache();
		Scanner input = new Scanner(System.in);

		while (input.hasNext()) {
			System.out.println("(R)ead, (W)rite, or (D)isplay Cache?");
			String command = input.next();
			System.out.println(command);
			Address address;
			short data;

			switch (command) {
				case "R": // Read
					System.out.println("What address would you like to read?");
					String inputRAddress = input.next();
					address = new Address(Integer.parseInt(inputRAddress, 16));
					System.out.println(inputRAddress);
					simulator.read(address);
					break;
				case "W": // Write
					System.out.println("What address would you like to write to?");
					String inputWAddress = input.next();
					address = new Address(Integer.parseInt(inputWAddress, 16));
					System.out.println(inputWAddress);
					System.out.println("What data would you like to write at that address?");
					String inputData = input.next();
					data = Short.parseShort(inputData, 16);
					System.out.println(inputData);
					simulator.write(address, data);
					break;
				case "D": // Display
					simulator.display();
					break;
			}
		}
	}
}

class Address {
	public int address;
	public int tag;
	public int slot;
	public int offset;
	public int beginAddress;

	public Address(int address) {
		this.address = address;
		this.tag = address >>> 8;
		this.slot = (address & 0xF0) >>> 4;
		this.offset = (address & 0xF);
		this.beginAddress = (address & 0xFFF0);
	}

}

class Block {
	public int dirty = 0;
	public int valid = 0;
	public int tag;
	public int slot;
	public short[] data = new short[16]; // Block size is 16

	public Block(short slot) {
		this.slot = slot;
		for (int i = 0; i < data.length; i++) {
			this.data[i] = (short) 0; // Initialize to all zeros
		}
	}

	public boolean isDirty() { return this.dirty == 1; }
	public void setDirty(int num) { this.dirty = num; }

	public boolean isValid() { return this.valid == 1; }
	public void setValid(int num) {	this.valid = num; }

	public boolean checkTag(int tag) { return this.tag == tag; }
	public void updateTag(int tag) { this.tag = tag; }

	public short getData(int idx) { return this.data[idx]; }
	public void setData(int idx, short val) { this.data[idx] = val; }

	public void printBlock() {
		System.out.print("  " + Integer.toHexString(this.slot).toUpperCase() + "     " + this.valid + "     " + this.tag + "      ");
		for (int i = 0; i < this.data.length; i++) {
			if (i == this.data.length - 1) { // No trailing white space for last one
				System.out.print(Integer.toHexString(this.data[i]));
			} else {
				System.out.print(Integer.toHexString(this.data[i]) + " ");
			}
		}
	}

}
