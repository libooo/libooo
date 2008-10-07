package com.okay.validate.srm;


import java.io.PrintStream;

class UnionFind
{

    UnionFind(int i)
    {
        parent = new int[i];
        rank = new int[i];
        System.out.println((new StringBuilder()).append("Creating a UF DS for ").append(i).append(" elements").toString());
        for(int j = 0; j < i; j++)
        {
            parent[j] = j;
            rank[j] = 0;
        }

    }

    int Find(int i)
    {
        for(; parent[i] != i; i = parent[i]);
        return i;
    }

    int UnionRoot(int i, int j)
    {
        if(i == j)
            return -1;
        if(rank[i] > rank[j])
        {
            parent[j] = i;
            return i;
        }
        parent[i] = j;
        if(rank[i] == rank[j])
            rank[j]++;
        return j;
    }

    int rank[];
    int parent[];
}