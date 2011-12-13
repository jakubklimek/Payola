package s2js.compiler


class VariableSpecs extends CompilerFixtureSpec
{
    describe("Variables") {
        it("can have literal values") {
            configMap =>
                scalaCode {
                    """
                        package foo

                        class A {
                            def m1() {
                                val a = "foo"
                                val b = 1
                                val c = true
                                val d = 1.0
                            }
                        }
                    """
                } shouldCompileTo {
                    """
                        goog.provide('foo.A');

                        foo.A = function() {
                            var self = this;
                        };

                        foo.A.prototype.m1 = function() {
                            var self = this;
                            var a = 'foo';
                            var b = 1;
                            var c = true;
                            var d = 1.0;
                        };
                        foo.A.prototype.metaClass_ = new s2js.MetaClass('foo.A', []);
                    """
                }
        }

        it("can have instance values") {
            configMap =>
                scalaCode {
                    """
                        package foo {
                            class A {
                                def m1() {
                                    val a = new B
                                }
                            }
                            
                            class B
                        }
                    """
                } shouldCompileTo {
                    """
                        goog.provide('foo.A');
                        goog.provide('foo.B');

                        foo.A = function() {
                            var self = this;
                        };
                        foo.A.prototype.m1 = function() {
                            var self = this;
                            var a = new foo.B();
                        };
                        foo.A.prototype.metaClass_ = new s2js.MetaClass('foo.A', []);
                        
                        foo.B = function() {
                            var self = this;
                        };
                        foo.B.prototype.metaClass_ = new s2js.MetaClass('foo.B', []);
                    """
                }
        }

        it("can have parameter values") {
            configMap =>
                scalaCode {
                    """
                        package foo {
                            class A {
                                def m1(y: String) {
                                    val a = y
                                }
                            }
                        }
                    """
                } shouldCompileTo {
                    """
                        goog.provide('foo.A');

                        foo.A = function() {
                            var self = this;
                        };
                        foo.A.prototype.m1 = function(y) {
                            var self = this;
                            var a = y;
                        };
                        foo.A.prototype.metaClass_ = new s2js.MetaClass('foo.A', []);
                    """
                }
        }

        it("can have function return values") {
            configMap =>
                scalaCode {
                    """
                        package foo {
                            class A {
                                def m1() = "foo"
                                def m2() {
                                    var a = m1();
                                }
                            }
                        }
                    """
                } shouldCompileTo {
                    """
                        goog.provide('foo.A');

                        foo.A = function() {
                            var self = this;
                        };
                        foo.A.prototype.m1 = function() {
                            var self = this;
                            return 'foo';
                        };
                        foo.A.prototype.m2 = function() {
                            var self = this;
                            var a = self.m1();
                        };
                        foo.A.prototype.metaClass_ = new s2js.MetaClass('foo.A', []);
                    """
                }
        }

        it("can have expression values") {
            configMap =>
                scalaCode {
                    """
                        package foo {
                            class A {
                                def m1(x: Int) {
                                    var a = x + 5
                                    var b = x == 5
                                    var c = ((9 * a) / (2 + a))
                                }
                            }
                        }
                    """
                } shouldCompileTo {
                    """
                        goog.provide('foo.A');

                        foo.A = function() {
                            var self = this;
                        };
                        foo.A.prototype.m1 = function(x) {
                            var self = this;
                            var a = (x + 5);
                            var b = (x == 5);
                            var c = ((9 * a) / (2 + a));
                        };
                        foo.A.prototype.metaClass_ = new s2js.MetaClass('foo.A', []);
                    """
                }
        }

        it("can have function values") {
            configMap =>
                scalaCode {
                    """
                        package foo {
                            class A {
                                def m1() {
                                    val a = (b: String) => { "foo" + b }
                                }
                            }
                        }
                    """
                } shouldCompileTo {
                    """
                        goog.provide('foo.A');

                        foo.A = function() {
                            var self = this;
                        };
                        foo.A.prototype.m1 = function() {
                            var self = this;
                            var a = function(b) { return ('foo' + b); };
                        };
                        foo.A.prototype.metaClass_ = new s2js.MetaClass('foo.A', []);
                    """
                }
        }
    }
}

